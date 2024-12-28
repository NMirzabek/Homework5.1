package uz.pdp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Vector;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long chatId;
    private Status status = Status.START;
    private Payment payment;
    private Payment currentPayment;
    List<Payment> payments = new Vector<>();
}
