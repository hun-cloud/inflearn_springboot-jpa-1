package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @DisplayName("상품주문")
    @Test
    void 상품주문() {
        // given
        Member member = createMember();

        Book book = createBook();

        int orderCount = 2;

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        // then

        Order getOrder = orderRepository.findOne(orderId);

        assertThat(OrderStatus.ORDER).isEqualTo(getOrder.getStatus());
        assertThat(1).isEqualTo(getOrder.getOrderItems().size());
        assertThat(10000 * 2).isEqualTo(getOrder.getTotalPrice());
        assertThat(8).isEqualTo(book.getStockQuantity());
    }

    @DisplayName("")
    @Test
    void 상품주문_재고수량초과() {
        // given
        Member member = createMember();

        Item book = createBook();
        int orderCount = 11;
        // when // then
        assertThatThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount))
                .isInstanceOf(NotEnoughStockException.class);
    }
    
    @DisplayName("")
    @Test
    void 주문취소() {
        // given
        Member member = createMember();
        Book book = createBook();

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        // when
        orderService.cancelOrder(orderId);

        // then
        Order findOne = orderRepository.findOne(orderId);

        assertThat(OrderStatus.CANCEL).isEqualTo(findOne.getStatus());
        assertThat(10).isEqualTo(book.getStockQuantity());

    }

    private Book createBook() {
        Book book = new Book();
        book.setName("JPA");
        book.setPrice(10000);
        book.setStockQuantity(10);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "경기", "123-123"));
        em.persist(member);
        return member;
    }
}