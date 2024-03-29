package sample.cafekiosk.spring.api.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sample.cafekiosk.spring.api.service.mail.MailService;
import sample.cafekiosk.spring.domain.order.Order;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.order.OrderStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * 메일 부분에는 트랜잭션을 안거는것이 좋다
 * DB조회를 할 때 Connection 자원을 가지고 있으니, 메일 전송같이 긴 네트워크 작업이 있는 서비스에서는
 * 트랜잭션을 걸지 않는게 좋다. 조회할 때는 조회하는 쿼리에서 리포지단에서 걸리기 떄문에 필요없다.
 */
@RequiredArgsConstructor
@Service
public class OrderStatisticsService {

    private final OrderRepository orderRepository;
    private final MailService mailService;

    public boolean sendOrderStatisticsMail(LocalDate orderDate, String email) {
        List<Order> orders = orderRepository.findOrdersBy(
            orderDate.atStartOfDay(),
            orderDate.plusDays(1).atStartOfDay(),
            OrderStatus.PAYMENT_COMPLETED
        );

        int totalAmount = orders.stream()
            .mapToInt(Order::getTotalPrice)
            .sum();

        boolean result = mailService.sendMail(
            "no-reply@cafekiosk.com",
            email,
            String.format("[매출통계] %s", orderDate),
            String.format("총 매출 합계는 %s원입니다.", totalAmount)
        );

        if (!result) {
            throw new IllegalArgumentException("매출 통계 메일 전송에 실패했습니다.");
        }

        return true;
    }
}
