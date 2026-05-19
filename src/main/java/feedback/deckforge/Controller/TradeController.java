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
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class TradeController {

    private final TradeService tradeService;
    private final UserService userService;
    private TradeCollectionService tradeCollectionService;
    private CardService cardService;

    public TradeController(TradeService tradeService, UserService userService, TradeCollectionService tradeCollectionService, CardService cardService){
        this.tradeService = tradeService;
        this.userService = userService;
        this.tradeCollectionService = tradeCollectionService;
        this.cardService = cardService;
    }

    @GetMapping("/trades")
    public String showTradeDashboard(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        List<Trade> userTrades = tradeService.findAllTradesByUserId(loggedInUser.getUserID());

        List<Trade> tradeHistory = userTrades.stream().filter(t -> t.getTradeStatus() == TradeStatus.COMPLETED ||
                t.getTradeStatus() == TradeStatus.CANCELLED ||
                t.getTradeStatus() == TradeStatus.DECLINED).collect(Collectors.toList());

        model.addAttribute("tradeHistory", tradeHistory);
        model.addAttribute("currentUserId", loggedInUser.getUserID());

        return "TradeController/trade-dashboard";
    }

    @GetMapping("/incoming")
    public String showIncomingTrades(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        List<Trade> incomingTrades = tradeService.findAllTradesByUserId(loggedInUser.getUserID())
                .stream().filter(t -> t.getTradeStatus() == TradeStatus.PENDING && t.getReceiver()
                        .getUserID() == loggedInUser.getUserID()).collect(Collectors.toList());

        model.addAttribute("incomingTrades", incomingTrades);
        return "TradeController/trade-incoming";
    }

    @GetMapping("/outgoing")
    public String showOutgoingTrades(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // Find all pending trades where the logged-in user is the initiator
        List<Trade> outgoingTrades = tradeService.findAllTradesByUserId(loggedInUser.getUserID())
                .stream()
                .filter(t -> t.getTradeStatus() == TradeStatus.PENDING &&
                        t.getInitiator().getUserID() == loggedInUser.getUserID())
                .collect(Collectors.toList());

        model.addAttribute("outgoingTrades", outgoingTrades);

        // Make sure this matches the exact name and folder of your outgoing HTML file
        return "TradeController/trade-outgoing";
    }

    @GetMapping("/ongoing")
    public String showOngoingTrades(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        List<Trade> ongoingTrades = tradeService.findAllTradesByUserId(loggedInUser.getUserID()).stream()
                .filter(t-> t.getTradeStatus() == TradeStatus.WAITING_FOR_PARTNER)
                .collect(Collectors.toList());

        model.addAttribute("ongoingTrades", ongoingTrades);
        model.addAttribute("currentUserId", loggedInUser.getUserID());

        return "TradeController/trade-ongoing";
    }


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