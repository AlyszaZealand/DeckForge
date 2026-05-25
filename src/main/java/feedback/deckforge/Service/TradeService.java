package feedback.deckforge.Service;

import feedback.deckforge.Exceptions.InsufficientCardsException;
import feedback.deckforge.Exceptions.TradeNotFoundException;
import feedback.deckforge.Exceptions.UnauthorizedException;
import feedback.deckforge.Model.*;
import feedback.deckforge.Model.DTO.TradeCardDTO;
import feedback.deckforge.Model.DTO.TradeViewDTO;
import feedback.deckforge.Model.Enum.TradeStatus;
import feedback.deckforge.Service.RepoInterfaces.ITradeRepository;
import feedback.deckforge.Service.Validation.TradeValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TradeService {

    private final ITradeRepository tradeRepository;
    private final CollectionService collectionService;
    private final TradeCollectionService tradeCollectionService;
    private final TradeValidation tradeValidation;
    private final UserService userService;

    public TradeService(ITradeRepository tradeRepository,
                        CollectionService collectionService,
                        TradeCollectionService tradeCollectionService,
                        TradeValidation tradeValidation, UserService userService){
        this.tradeRepository = tradeRepository;
        this.collectionService = collectionService;
        this.tradeCollectionService = tradeCollectionService;
        this.tradeValidation = tradeValidation;
        this.userService = userService;
    }

    // ==========================================
    // 1. OPRET FORSLAG (Med TradeValidation)
    // ==========================================
    public ValidationResult proposeTrade(Trade trade) {
        // Her kalder vi din nye valideringsklasse!
        ValidationResult result = tradeValidation.validateProposal(trade);

        if (!result.hasErrors()) {
            trade.setTradeStatus(TradeStatus.PENDING);
            tradeRepository.saveTrade(trade);
        }
        return result;
    }

    // ==========================================
    // 2. ACCEPTER ELLER AFVIS
    // ==========================================
    public void respondToTrade(int tradeId, boolean isAccepted, int currentUserId) {
        Trade trade = tradeRepository.findTradeById(tradeId)
                .orElseThrow(() -> new TradeNotFoundException("Handlen blev ikke fundet."));

        if (trade.getReceiver().getUserID() != currentUserId){
            throw new SecurityException("Du har ikke tilladelse til at besvare dette bytte forslag");
        }
        if (isAccepted) {
            trade.setTradeStatus(TradeStatus.ACCEPTED);

            tradeRepository.updateTradeStatus(tradeId, TradeStatus.ACCEPTED);

            // 3. NU kan vi trygt tjekke de andre handler, da Trade A med sikkerhed er 'ACCEPTED' i databasen
            cancelConflictingPendingTrades(trade.getInitiator().getUserID());
            cancelConflictingPendingTrades(trade.getReceiver().getUserID());

        } else {
            trade.setTradeStatus(TradeStatus.DECLINED);
            tradeRepository.updateTradeStatus(tradeId, TradeStatus.DECLINED);
        }
    }

    // ==========================================
    // 3. ANNULLER BYTTE
    // ==========================================
    public void cancelTrade(int tradeID, int currentUserId) {
        Trade trade = tradeRepository.findTradeById(tradeID).orElseThrow(() -> new TradeNotFoundException("Byttehandlen kunne ikke findes"));

        if (trade.getInitiator().getUserID() != currentUserId){
            throw new UnauthorizedException("Du kan kun annullere dine egne handler.");
        }

        // Validering: Man kan ikke annullere et bytte, der er afvist eller allerede færdigt
        if (trade.getTradeStatus() == TradeStatus.DECLINED || trade.getTradeStatus() == TradeStatus.COMPLETED) {
            return;
        }

        // Hvis handlen var Accepteret, skal kortene lægges tilbage (reservation ophæves)
        if (trade.getTradeStatus() == TradeStatus.ACCEPTED || trade.getTradeStatus() == TradeStatus.WAITING_FOR_PARTNER) {
            for (Card card : trade.getOfferedCards()) {
                tradeCollectionService.addCardsToTradeCollection(trade.getInitiator().getUserID(),card.getCardID(),1);
            }
            for (Card card : trade.getRequestedCards()) {
                tradeCollectionService.addCardsToTradeCollection(trade.getReceiver().getUserID(),card.getCardID(),1);
            }
        }

        trade.setTradeStatus(TradeStatus.CANCELLED);
        trade.setCompletedDate(LocalDateTime.now());
        tradeRepository.updateTradeStatus(tradeID, trade.getTradeStatus());
    }

    // ==========================================
    // 4. FÆRDIGGØR IRL BYTTE
    // ==========================================
    public void finalizeTrade(int tradeID, int userID) {
        Trade trade = tradeRepository.findTradeById(tradeID)
                .orElseThrow(() -> new TradeNotFoundException("Byttehandlen kunne ikke findes"));

        // Validering: Kun muligt hvis handlen er ACCEPTED eller WAITING_FOR_PARTNER
        if (trade.getTradeStatus() != TradeStatus.ACCEPTED && trade.getTradeStatus() != TradeStatus.WAITING_FOR_PARTNER) {
            return;
        }

        // 1. Registrer hvem der har bekræftet
        if (trade.getInitiator().getUserID() == userID) {
            trade.setInitiatorConfirmed(true);
        } else if (trade.getReceiver().getUserID() == userID) {
            trade.setReceiverConfirmed(true);
        }

        // 2. Tjek om BEGGE har bekræftet
        if (trade.isInitiatorConfirmed() && trade.isReceiverConfirmed()) {

            // Sæt status til COMPLETED for at fjerne den fra "låste" handler, mens vi tjekker
            trade.setTradeStatus(TradeStatus.COMPLETED);

            // =========================================================
            // LAST LINE OF DEFENSE: Er kortene stadig ledige?
            // (Forhindrer 'Race Condition' hvis to accepterer samtidig)
            // =========================================================
            ValidationResult finalCheck = tradeValidation.validateInventory(trade);

            if (finalCheck.hasErrors()) {
                // Hvis kortene ikke er der længere: Afbryd handlen, sæt til DECLINED og kast en fejl!
                trade.setTradeStatus(TradeStatus.DECLINED);
                tradeRepository.updateTradeStatusAndConfirmations(
                        tradeID, TradeStatus.DECLINED, trade.isInitiatorConfirmed(), trade.isReceiverConfirmed());

                throw new InsufficientCardsException("Handlen blev automatisk afbrudt: " + finalCheck.getErrors().get(0));
            }

            // =========================================================
            // ALT ER OKAY: Udfør den fysiske udveksling
            // =========================================================
            trade.setCompletedDate(LocalDateTime.now());

            // DEN STORE UDVEKSLING mellem personlige samlinger
            for (Card card : trade.getOfferedCards()) {
                collectionService.removeCards(trade.getInitiator().getUserID(), card.getCardID(), 1);
                tradeCollectionService.removeCardsFromTradeCollection(trade.getInitiator().getUserID(),card.getCardID(),1);
                collectionService.addCards(trade.getReceiver().getUserID(), card.getCardID(), 1);
                tradeCollectionService.syncTradeCollectionWithPrivateCollection(trade.getInitiator().getUserID(), card.getCardID());
            }

            for (Card card : trade.getRequestedCards()) {
                collectionService.removeCards(trade.getReceiver().getUserID(), card.getCardID(), 1);
                tradeCollectionService.removeCardsFromTradeCollection(trade.getInitiator().getUserID(),card.getCardID(),1);
                collectionService.addCards(trade.getInitiator().getUserID(), card.getCardID(), 1);
                tradeCollectionService.syncTradeCollectionWithPrivateCollection(trade.getReceiver().getUserID(), card.getCardID());
            }

        } else {
            // Hvis kun én har accepteret indtil videre
            trade.setTradeStatus(TradeStatus.WAITING_FOR_PARTNER);
        }

        // 3. GEM DET HELE I DATABASEN (Status + Bekræftelser)
        tradeRepository.updateTradeStatusAndConfirmations(
                tradeID,
                trade.getTradeStatus(),
                trade.isInitiatorConfirmed(),
                trade.isReceiverConfirmed()
        );
    }

    public void populateTradeCards(Trade trade,
                                   List<Integer> offeredIds, List<Integer> offeredQuantities,
                                   List<Integer> requestedIds, List<Integer> requestedQuantities,
                                   CardService cardService) {

        if (offeredIds != null && offeredQuantities != null) {
            for (int i = 0; i < offeredIds.size(); i++) {
                int quantity = offeredQuantities.get(i);
                if (quantity > 0) {
                    final int cardId = offeredIds.get(i);
                    cardService.getCardById(cardId).ifPresent(card -> {
                        for (int j = 0; j < quantity; j++) {
                            trade.getOfferedCards().add(card);
                        }
                    });
                }
            }
        }

        if (requestedIds != null && requestedQuantities != null) {
            for (int i = 0; i < requestedIds.size(); i++) {
                int quantity = requestedQuantities.get(i);
                if (quantity > 0) {
                    final int cardId = requestedIds.get(i);
                    cardService.getCardById(cardId).ifPresent(card -> {
                        for (int j = 0; j < quantity; j++) {
                            trade.getRequestedCards().add(card);
                        }
                    });
                }
            }
        }
    }

    private void cancelConflictingPendingTrades(int userId) {
        // Hent alle PENDING handler for brugeren
        List<Trade> pendingTrades = tradeRepository.findAllTradesByUserId(userId).stream()
                .filter(t -> t.getTradeStatus() == TradeStatus.PENDING)
                .collect(Collectors.toList());

        for (Trade pendingTrade : pendingTrades) {
            // Vi bruger Inventory-tjek fra Valideringsklassen!
            ValidationResult result = tradeValidation.validateInventory(pendingTrade);

            // Hvis valideringen fejler, betyder det, at de ikke længere har kopier nok!
            if (result.hasErrors()) {
                pendingTrade.setTradeStatus(TradeStatus.DECLINED);
                tradeRepository.updateTradeStatus(pendingTrade.getTradeId(), TradeStatus.DECLINED);
            }
        }
    }



    public List<TradeCardDTO> getAvailableInitiatorCards(
            int userId, String search, String rarity, String type, CollectionService collectionService) {

        // 1. Hent de rå data fra samlingen via CollectionService
        List<CollectionItem> rawMyItems = collectionService.getFilteredCollectionItems(userId, search, rarity, type);

        // 2. Map til DTO og filtrer de låste kort fra (Forretningslogik)
        return rawMyItems.stream().map(item -> {
                    int locked = (int) tradeValidation.getLockedQuantity(userId, item.getCard().getCardID(), -1);
                    return new TradeCardDTO(item.getCard(), item.getQuantity(), item.getQuantity() - locked);
                })
                .filter(dto -> dto.getAvailableQuantity() > 0)
                .collect(Collectors.toList());
    }

    public List<TradeCardDTO> getAvailableReceiverCards(
            int userId, String search, String rarity, String type, TradeCollectionService tradeCollectionService) {

        // 1. Hent de rå data fra tradelisten via TradeCollectionService
        List<TradeCollectionItem> rawPartnerItems = tradeCollectionService.getFilteredTradeCollectionItems(userId, search, rarity, type);

        // 2. Map til DTO og filtrer de låste kort fra (Forretningslogik)
        return rawPartnerItems.stream().map(item -> {
                    int locked = (int) tradeValidation.getLockedQuantity(userId, item.getCard().getCardID(), -1);
                    return new TradeCardDTO(item.getCard(), item.getQuantity(), item.getQuantity() - locked);
                })
                .filter(dto -> dto.getAvailableQuantity() > 0)
                .collect(Collectors.toList());
    }

    private TradeViewDTO mapToViewDTO(Trade trade, int currentUserId) {
        TradeViewDTO dto = new TradeViewDTO();
        dto.setTradeId(trade.getTradeId());
        dto.setTradeDate(trade.getTradeDate());
        dto.setTradeStatus(trade.getTradeStatus());

        boolean isInitiator = trade.getInitiator().getUserID() == currentUserId;

        // **THE FIX: Ensure we have the full User object to get the username**
        // We fetch the partner's user ID, then ask the UserService for the full User object.
        int partnerId = isInitiator ? trade.getReceiver().getUserID() : trade.getInitiator().getUserID();
        User partnerUser = userService.getUserByID(partnerId);

        dto.setPartnerUsername(partnerUser.getUsername());

        // 1. Get the raw lists of cards based on your perspective in the trade
        List<Card> rawGive = isInitiator ? trade.getOfferedCards() : trade.getRequestedCards();
        List<Card> rawGet = isInitiator ? trade.getRequestedCards() : trade.getOfferedCards();

        // 2. Pass the raw lists into your groupCards method
        dto.setCardsYouGive(groupCards(rawGive));
        dto.setCardsYouGet(groupCards(rawGet));

        // Resolve confirmation states for Ongoing IRL trades
        if (isInitiator) {
            dto.setRequiresYourConfirmation(!trade.isInitiatorConfirmed());
            dto.setWaitingForPartnerConfirmation(trade.isInitiatorConfirmed() && !trade.isReceiverConfirmed());
        } else {
            dto.setRequiresYourConfirmation(!trade.isReceiverConfirmed());
            dto.setWaitingForPartnerConfirmation(trade.isReceiverConfirmed() && !trade.isInitiatorConfirmed());
        }

        return dto;
    }

    public List<TradeViewDTO> getIncomingTradesForUser(int userId) {
        return findAllTradesByUserId(userId).stream()
                .filter(t -> t.getTradeStatus() == TradeStatus.PENDING && t.getReceiver().getUserID() == userId)
                .map(t -> mapToViewDTO(t, userId))
                .collect(Collectors.toList());
    }

    public List<TradeViewDTO> getOutgoingTradesForUser(int userId) {
        return findAllTradesByUserId(userId).stream()
                .filter(t -> t.getTradeStatus() == TradeStatus.PENDING && t.getInitiator().getUserID() == userId)
                .map(t -> mapToViewDTO(t, userId))
                .collect(Collectors.toList());
    }

    public List<TradeViewDTO> getOngoingTradesForUser(int userId) {
        return findAllTradesByUserId(userId).stream()
                .filter(t -> t.getTradeStatus() == TradeStatus.ACCEPTED || t.getTradeStatus() == TradeStatus.WAITING_FOR_PARTNER)
                .map(t -> mapToViewDTO(t, userId))
                .collect(Collectors.toList());
    }


    public List<TradeViewDTO> getTradeHistoryForUser(int userId, String sort) {
        // 1. Fetch and map the list just like before
        List<TradeViewDTO> history = findAllTradesByUserId(userId).stream()
                .filter(t -> t.getTradeStatus() == TradeStatus.COMPLETED ||
                        t.getTradeStatus() == TradeStatus.CANCELLED ||
                        t.getTradeStatus() == TradeStatus.DECLINED)
                .map(t -> mapToViewDTO(t, userId))
                .collect(Collectors.toList());

        // 2. Sort the list based on the user's choice
        if ("asc".equalsIgnoreCase(sort)) {
            // Oldest first
            history.sort(Comparator.comparing(TradeViewDTO::getTradeDate));
        } else {
            // Default to Newest first (descending)
            history.sort(Comparator.comparing(TradeViewDTO::getTradeDate).reversed());
        }

        return history;
    }


    private List<TradeCardDTO> groupCards(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Sort the cards by their ID so duplicates are next to each other
        List<Card> sortedCards = new ArrayList<>(cards);
        sortedCards.sort(Comparator.comparingInt(Card::getCardID));

        List<TradeCardDTO> groupedResult = new ArrayList<>();

        // 2. Start counting the first card
        Card currentCard = sortedCards.get(0);
        int count = 1;

        // 3. Loop through the rest of the list
        for (int i = 1; i < sortedCards.size(); i++) {
            Card nextCard = sortedCards.get(i);

            if (nextCard.getCardID() == currentCard.getCardID()) {
                // It's the same card, increase the count
                count++;
            } else {
                // It's a new card! Save the previous one and reset the counter
                groupedResult.add(new TradeCardDTO(currentCard, count, 0));
                currentCard = nextCard;
                count = 1;
            }
        }

        // 4. Don't forget to add the very last group after the loop finishes!
        groupedResult.add(new TradeCardDTO(currentCard, count, 0));

        return groupedResult;
    }



    // ==========================================
    // 5. STANDARD HENTE-METODER
    // ==========================================
    public void deleteTrade(int tradeID){ tradeRepository.deleteTrade(tradeID); }
    public Optional<Trade> findTradeById(int tradeID){ return tradeRepository.findTradeById(tradeID); }
    public List<Trade> findTradesByInitiatorId(int initiatorId){ return tradeRepository.findTradesByInitiatorId(initiatorId); }
    public List<Trade> findTradesByReceiverId(int receiverId){ return tradeRepository.findTradesByReceiverId(receiverId); }
    public List<Trade> findAllTradesByUserId(int userID){ return tradeRepository.findAllTradesByUserId(userID); }
}
