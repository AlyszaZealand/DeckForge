package feedback.deckforge.Controller;

import feedback.deckforge.Model.*;
import feedback.deckforge.Model.DTO.TradeCardDTO;
import feedback.deckforge.Model.DTO.TradeViewDTO;
import feedback.deckforge.Model.Enum.TradeStatus;
import feedback.deckforge.Service.*;
import feedback.deckforge.Service.Validation.ValidationResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TradeController {

    private final TradeService tradeService;
    private final UserService userService;
    private TradeCollectionService tradeCollectionService;
    private CardService cardService;
    private CollectionService collectionService;

    public TradeController(TradeService tradeService, UserService userService,
                           TradeCollectionService tradeCollectionService, CardService cardService,
                           CollectionService collectionService){
        this.tradeService = tradeService;
        this.userService = userService;
        this.tradeCollectionService = tradeCollectionService;
        this.cardService = cardService;
        this.collectionService = collectionService;
    }


    @GetMapping("/trades")
    public String showTradeDashboard(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // Fetch ALL trades for this user once
        List<Trade> userTrades = tradeService.findAllTradesByUserId(loggedInUser.getUserID());

        // Count Incoming Proposals (Pending & User is the Receiver)
        long incomingTradeCount = userTrades.stream()
                .filter(t -> t.getTradeStatus() == TradeStatus.PENDING && t.getReceiver().getUserID() == loggedInUser.getUserID())
                .count();

        // Count Ongoing Trades (Accepted OR waiting for the second confirmation)
        long ongoingTradeCount = userTrades.stream()
                .filter(t -> t.getTradeStatus() == TradeStatus.ACCEPTED ||
                        t.getTradeStatus() == TradeStatus.WAITING_FOR_PARTNER)
                .count();

        // Add to the model
        model.addAttribute("currentUserId", loggedInUser.getUserID());
        model.addAttribute("incomingTradeCount", incomingTradeCount);
        model.addAttribute("ongoingTradeCount", ongoingTradeCount);

        return "TradeController/trade-dashboard";
    }


    @GetMapping("/incoming")
    public String showIncomingTrades(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // Let the service handle the logic and mapping
        List<TradeViewDTO> incomingTrades = tradeService.getIncomingTradesForUser(loggedInUser.getUserID());

        model.addAttribute("incomingTrades", incomingTrades);
        return "TradeController/trade-incoming";
    }

    @GetMapping("/outgoing")
    public String showOutgoingTrades(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        List<TradeViewDTO> outgoingTrades = tradeService.getOutgoingTradesForUser(loggedInUser.getUserID());

        model.addAttribute("outgoingTrades", outgoingTrades);
        return "TradeController/trade-outgoing";
    }

    @GetMapping("/ongoing")
    public String showOngoingTrades(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        List<TradeViewDTO> ongoingTrades = tradeService.getOngoingTradesForUser(loggedInUser.getUserID());

        model.addAttribute("ongoingTrades", ongoingTrades);
        return "TradeController/trade-ongoing";
    }

    @GetMapping("/trade_history")
    public String showTradeHistory(
            @RequestParam(name = "sort", defaultValue = "desc") String sort,
            HttpSession session, Model model){

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // Pass the sort parameter into your service method
        List<TradeViewDTO> tradeHistory = tradeService.getTradeHistoryForUser(loggedInUser.getUserID(), sort);

        model.addAttribute("tradeHistory", tradeHistory);
        model.addAttribute("currentUserId", loggedInUser.getUserID());

        // Pass the current sort string to Thymeleaf so the dropdown remembers what is selected
        model.addAttribute("currentSort", sort);

        return "TradeController/trade-history";
    }

    /*@GetMapping("/trade_history")
    public String showTradeHistory(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        List<Trade> userTrades = tradeService.findAllTradesByUserId(loggedInUser.getUserID());

        List<Trade> tradeHistory = userTrades.stream()
                .filter(t -> t.getTradeStatus() == TradeStatus.COMPLETED ||
                        t.getTradeStatus() == TradeStatus.CANCELLED)
                .collect(Collectors.toList());

        // FIX: Fetch BOTH users since we need to dynamically check who the partner is
        for (Trade trade : tradeHistory) {
            trade.setInitiator(userService.getUserByID(trade.getInitiator().getUserID()));
            trade.setReceiver(userService.getUserByID(trade.getReceiver().getUserID()));
        }

        model.addAttribute("tradeHistory", tradeHistory);
        model.addAttribute("currentUserId", loggedInUser.getUserID());

        return "TradeController/trade-history";
    }*/


    @PostMapping("/respond")
    public String respondToTrade(@RequestParam("tradeId") int tradeId,
                                 @RequestParam("isAccepted") boolean isAccepted,
                                 @RequestParam(name = "redirectPath", defaultValue = "/trades") String redirectPath){
        tradeService.respondToTrade(tradeId, isAccepted);
        return "redirect:" + redirectPath;
    }

    @PostMapping("/cancel")
    public String cancelTrade(@RequestParam("tradeId") int tradeId,
                              @RequestParam(name = "redirectPath", defaultValue = "/trades") String redirectPath){
        tradeService.cancelTrade(tradeId);
        return "redirect:" + redirectPath;
    }

    @PostMapping("/finalize")
    public String finalizeTrade(@RequestParam("tradeId") int tradeId,
                                HttpSession session,
                                @RequestParam(name = "redirectPath", defaultValue = "/trades") String redirectPath){
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        tradeService.finalizeTrade(tradeId, loggedInUser.getUserID());
        return "redirect:" + redirectPath;
    }

    // ==========================================
    // TRIN 1: GET - Søgning og Filtrering
    // ==========================================
    @GetMapping("/propose")
    public String showProposeTradeForm(
            @RequestParam(value = "receiverId", required = false) Integer receiverId,
            @RequestParam(value = "searchOffered", required = false) String searchOffered,
            @RequestParam(value = "rarityOffered", required = false) String rarityOffered,
            @RequestParam(value = "typeOffered", required = false) String typeOffered,
            @RequestParam(value = "searchRequested", required = false) String searchRequested,
            @RequestParam(value = "rarityRequested", required = false) String rarityRequested,
            @RequestParam(value = "typeRequested", required = false) String typeRequested,
            HttpSession session, Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // 1. Hent færdigbehandlede DTO'er for INITIATOR direkte fra TradeService
        List<TradeCardDTO> myAvailableItems = tradeService.getAvailableInitiatorCards(
                loggedInUser.getUserID(), searchOffered, rarityOffered, typeOffered, collectionService);

        model.addAttribute("myItems", myAvailableItems);
        model.addAttribute("searchOffered", searchOffered);
        model.addAttribute("rarityOffered", rarityOffered);
        model.addAttribute("typeOffered", typeOffered);

        // 2. Hent færdigbehandlede DTO'er for RECEIVER direkte fra TradeService
        List<TradeCardDTO> partnerAvailableItems = List.of();
        if (receiverId != null) {
            partnerAvailableItems = tradeService.getAvailableReceiverCards(
                    receiverId, searchRequested, rarityRequested, typeRequested, tradeCollectionService);
            model.addAttribute("selectedReceiverId", receiverId);
        }

        model.addAttribute("partnerItems", partnerAvailableItems);
        model.addAttribute("searchRequested", searchRequested);
        model.addAttribute("rarityRequested", rarityRequested);
        model.addAttribute("typeRequested", typeRequested);

        // 3. Dropdown-liste over andre brugere
        List<User> allOtherUsers = userService.getAllUsers().stream()
                .filter(u -> u.getUserID() != loggedInUser.getUserID())
                .collect(Collectors.toList());

        model.addAttribute("newTrade", new Trade());
        model.addAttribute("allOtherUsers", allOtherUsers);

        return "TradeController/trade-propose";
    }

    @PostMapping("/propose")
    public String submitTradeProposal(@ModelAttribute("newTrade") Trade trade,
                                      @RequestParam(value = "offeredCardId", required = false) List<Integer> offeredCardIds,
                                      @RequestParam(value = "offeredQuantity", required = false) List<Integer> offeredQuantities,
                                      @RequestParam(value = "requestedCardId", required = false) List<Integer> requestedCardIds,
                                      @RequestParam(value = "requestedQuantity", required = false) List<Integer> requestedQuantities,
                                      @RequestParam(value = "receiverId", required = false) Integer receiverId,
                                      HttpSession session, RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        trade.setInitiator(loggedInUser);
        trade.setTradeDate(LocalDateTime.now());

        if (receiverId != null) {
            trade.setReceiver(userService.getUserByID(receiverId));
        }

        // DELEGATION: Lad servicelaget pakke kort-mængderne ud
        tradeService.populateTradeCards(trade, offeredCardIds, offeredQuantities, requestedCardIds, requestedQuantities, cardService);

        // Validering og lagring
        ValidationResult result = tradeService.proposeTrade(trade);

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getErrors().get(0));
            return receiverId != null ? "redirect:/propose?receiverId=" + receiverId : "redirect:/propose";
        }

        redirectAttributes.addFlashAttribute("success", "Handelsforslag sendt!");
        return "redirect:/outgoing";
    }









}