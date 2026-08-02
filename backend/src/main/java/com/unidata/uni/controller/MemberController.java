package com.unidata.uni.controller;

import com.unidata.uni.dto.ApiResponse;
import com.unidata.uni.dto.AuthDtos.MemberOrderRequest;
import com.unidata.uni.entity.MemberOrder;
import com.unidata.uni.entity.User;
import com.unidata.uni.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/orders")
    public ApiResponse<MemberOrder> createOrder(@Valid @RequestBody MemberOrderRequest req) {
        return ApiResponse.ok(memberService.createOrder(req.plan()));
    }

    @PostMapping("/orders/{orderNo}/activate")
    public ApiResponse<User> activate(@PathVariable String orderNo) {
        return ApiResponse.ok(memberService.activateOrder(orderNo));
    }

    @GetMapping("/orders")
    public ApiResponse<List<MemberOrder>> myOrders() {
        return ApiResponse.ok(memberService.myOrders());
    }
}
