package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.EnumMap;
import java.util.Map;

public class TicketServiceImpl implements TicketService {

    private final TicketPaymentService paymentService;
    private final SeatReservationService reservationService;

    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateAccount(accountId);
        validateTicketRequests(ticketTypeRequests);

        // Process ticket request
        Map<TicketTypeRequest.Type, Integer> ticketCounts = countTickets(ticketTypeRequests);
        validateBusinessRules(ticketCounts);

        // Calculate total amount to be paid
        int totalAmount = calculateTotalPrice(ticketCounts);
        int totalSeats = calculateTotalSeats(ticketCounts);

        // Process payment and seat reservation
        paymentService.makePayment(accountId, totalAmount);
        reservationService.reserveSeat(accountId, totalSeats);
    }

    /**
     * Ensures the account ID is valid.
     */
    private void validateAccount(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Invalid account ID.");
        }
    }

    /**
     * Ensures the ticket requests are valid.
     */
    private void validateTicketRequests(TicketTypeRequest... requests) {
        if (requests == null || requests.length == 0) {
            throw new InvalidPurchaseException("At least one ticket must be requested.");
        }
    }

    /**
     * Counts ticket types from the request.
     */
    private Map<TicketTypeRequest.Type, Integer> countTickets(TicketTypeRequest... requests) {
        Map<TicketTypeRequest.Type, Integer> ticketCounts = new EnumMap<>(TicketTypeRequest.Type.class);
        for (TicketTypeRequest request : requests) {
            ticketCounts.put(
                    request.getTicketType(),
                    ticketCounts.getOrDefault(request.getTicketType(), 0) + request.getNoOfTickets()
            );
        }
        return ticketCounts;
    }

    /**
     * Validates business rules before processing the request.
     */
    private void validateBusinessRules(Map<TicketTypeRequest.Type, Integer> ticketCounts) {
        int totalTickets = ticketCounts.values().stream().mapToInt(Integer::intValue).sum();
        int adultTickets = ticketCounts.getOrDefault(TicketTypeRequest.Type.ADULT, 0);
        int childTickets = ticketCounts.getOrDefault(TicketTypeRequest.Type.CHILD, 0);
        int infantTickets = ticketCounts.getOrDefault(TicketTypeRequest.Type.INFANT, 0);

        if (totalTickets > 25) {
            throw new InvalidPurchaseException("Cannot purchase more than 25 tickets at a time.");
        }

        if (adultTickets == 0 && (childTickets > 0 || infantTickets > 0)) {
            throw new InvalidPurchaseException("Child or Infant tickets cannot be purchased without an Adult ticket.");
        }
    }

    /**
     * Calculates the total price to be paid.
     */
    private int calculateTotalPrice(Map<TicketTypeRequest.Type, Integer> ticketCounts) {
        return ticketCounts.getOrDefault(TicketTypeRequest.Type.ADULT, 0) * 25
                + ticketCounts.getOrDefault(TicketTypeRequest.Type.CHILD, 0) * 15;
    }

    /**
     * Calculates the total number of seats to be reserved (excluding infants).
     */
    private int calculateTotalSeats(Map<TicketTypeRequest.Type, Integer> ticketCounts) {
        return ticketCounts.getOrDefault(TicketTypeRequest.Type.ADULT, 0)
                + ticketCounts.getOrDefault(TicketTypeRequest.Type.CHILD, 0);
    }


}
