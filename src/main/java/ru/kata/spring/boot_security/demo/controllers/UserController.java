package ru.kata.spring.boot_security.demo.controllers;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.services.RoleService;
import ru.kata.spring.boot_security.demo.services.UserService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;

@Controller
public class UserController {


    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;


    public UserController(UserService userService, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;

    }

    @GetMapping({ "/admin"})
    public String showAllUsersFromAdmin(Model model, HttpServletRequest request) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("currentPath", request.getRequestURI());
        return "user";
    }

    @GetMapping({ "/user"})
    public String showAllUsersFromUser(Model model, Principal principal, HttpServletRequest request) {
        String username = principal.getName();
        model.addAttribute("users", userService.findByUsername(username));
        model.addAttribute("currentPath", request.getRequestURI());
        return "user";
    }

    @GetMapping("/admin/users/new")
    public ModelAndView newUser() {
        User user = new User();
        ModelAndView modelAndView = new ModelAndView("new_user");
        modelAndView.addObject("user", user);
        List<Role> roles = roleService.findAll();
        modelAndView.addObject("allRoles", roles);
        return modelAndView;
    }

    @RequestMapping(value = "/admin/users/save", method = RequestMethod.POST)
    public String saveUser(@ModelAttribute("user") User user, Model model) {
        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("usernameError","Имя пользователя занято");
            model.addAttribute("allRoles", roleService.findAll());
            return "edit";
        }
        User newUser = new User();
        String pwd = passwordEncoder.encode(user.getPassword());
        newUser.setUsername(user.getUsername());
        newUser.setPassword(pwd);
        newUser.setRoles(user.getRoles());
        userService.saveUser(newUser);
        return "redirect:/admin";
    }


    @GetMapping("/admin/users/edit/{username}")
    public ModelAndView editUser(@PathVariable (name = "username") String username) {
        User user = userService.findByUsername(username);
        ModelAndView modelAndView = new ModelAndView("edit");
        modelAndView.addObject("user", user);
        List<Role> roles = roleService.findAll();
        modelAndView.addObject("allRoles", roles);

        return modelAndView;

    }

    @PostMapping("/admin/users/update/{username}")
    public String updateUser(@PathVariable String username, @ModelAttribute("user") User user) {

        User user1 = userService.findByUsername(username);
        user1.setUsername(user.getUsername());
        user1.setRoles(user.getRoles());
        user1.setPassword(passwordEncoder.encode(user.getPassword()));

        userService.updateUser(user1);

        return "redirect:/admin";
    }

    @GetMapping("/admin/users/delete/{username}")
    public String removeUser(@PathVariable String username) {
        userService.deleteByUsername(username);
        return "redirect:/admin";
    }
}
