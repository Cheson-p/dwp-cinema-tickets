package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class TicketServiceImplTest {

    @Mock
    private TicketPaymentService ticketPaymentService;

    @Mock
    private SeatReservationService seatReservationService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    public void shouldRejectMoreThan25Tickets() {
        TicketTypeRequest tooManyTickets = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26);

        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, tooManyTickets));

        assertEquals("Cannot purchase more than 25 tickets at a time.", exception.getMessage());
    }

    @Test
    public void shouldRejectIfNoAdultTicketIsPresent() {
        TicketTypeRequest childTickets = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);

        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, childTickets));

        assertEquals("Child or Infant tickets cannot be purchased without an Adult ticket.", exception.getMessage());
    }

    @Test
    public void shouldRejectIfNoAdultTicketWithInfant() {
        TicketTypeRequest infantTickets = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, infantTickets));

        assertEquals("Child or Infant tickets cannot be purchased without an Adult ticket.", exception.getMessage());
    }

    @Test
    public void shouldRejectInvalidAccountId() {
        TicketTypeRequest adultTickets = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);

        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(0L, adultTickets));

        assertEquals("Invalid account ID.", exception.getMessage());
    }

}
