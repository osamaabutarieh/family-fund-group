package com.familyfund.controller;

import com.familyfund.model.Member;
import com.familyfund.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public String listMembers(Model model) {
        model.addAttribute("members", memberService.getAllMembers());
        return "members/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("member", new Member());
        return "members/form";
    }

    @PostMapping("/add")
    public String addMember(@Valid @ModelAttribute Member member,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "members/form";
        }

        try {
            memberService.createMember(member);
            redirectAttributes.addFlashAttribute("successMessage", "Member added successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/members";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("member", memberService.getMemberById(id));
            return "members/form";
        } catch (Exception e) {
            return "redirect:/members";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateMember(@PathVariable Long id,
                             @Valid @ModelAttribute Member member,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "members/form";
        }

        try {
            memberService.updateMember(id, member);
            redirectAttributes.addFlashAttribute("successMessage", "Member updated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/members";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteMember(@PathVariable Long id) {
        try {
            memberService.deleteMember(id);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
} 