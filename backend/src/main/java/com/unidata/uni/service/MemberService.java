package com.unidata.uni.service;

import com.unidata.uni.config.GlobalExceptionHandler.BizException;
import com.unidata.uni.entity.MemberOrder;
import com.unidata.uni.entity.User;
import com.unidata.uni.repository.MemberOrderRepository;
import com.unidata.uni.repository.UserRepository;
import com.unidata.uni.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MemberService {

    private static final Map<String, int[]> PLANS = Map.of(
            "MONTH", new int[]{1, 19},
            "QUARTER", new int[]{3, 49},
            "YEAR", new int[]{12, 168});

    private final MemberOrderRepository orderRepository;
    private final UserRepository userRepository;

    public MemberService(MemberOrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MemberOrder createOrder(String plan) {
        int[] cfg = PLANS.get(plan);
        if (cfg == null) {
            throw new BizException("套餐不存在");
        }
        MemberOrder order = new MemberOrder();
        order.setOrderNo("M" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        order.setUserId(CurrentUser.id());
        order.setPlan(plan);
        order.setMonths(cfg[0]);
        order.setAmount(BigDecimal.valueOf(cfg[1]));
        return orderRepository.save(order);
    }

    @Transactional
    public User activateOrder(String orderNo) {
        MemberOrder order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BizException("订单不存在"));
        if (!order.getUserId().equals(CurrentUser.id())) {
            throw new BizException("无权操作该订单");
        }
        if ("PAID".equals(order.getStatus())) {
            throw new BizException("订单已支付，请勿重复操作");
        }
        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);

        User user = userRepository.findById(order.getUserId()).orElseThrow();
        LocalDateTime base = user.isVip()
                ? user.getMembershipExpireAt()
                : LocalDateTime.now();
        user.setMembershipType("VIP");
        user.setMembershipExpireAt(base.plusMonths(order.getMonths()));
        return userRepository.save(user);
    }

    public List<MemberOrder> myOrders() {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(CurrentUser.id());
    }
}
