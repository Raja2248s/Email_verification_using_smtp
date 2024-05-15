package com.pixeltrice.springbootsendactivationlink;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@CrossOrigin(origins = "http://localhost:4200")
public class UserAccountController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;

    @Autowired
    private EmailService emailService;

    @RequestMapping(value="/register", method = RequestMethod.GET)
    public ModelAndView displayRegistration(ModelAndView modelAndView, UserEntity userEntity)
    {
        modelAndView.addObject("userEntity", userEntity);
        modelAndView.setViewName("register");
        return modelAndView;
    }
    
    
    
    @RequestMapping(value="/register", method = RequestMethod.POST)
    public ModelAndView registerUser(ModelAndView modelAndView, UserEntity userEntity)
    {

    	UserEntity existingUser = userRepository.findByEmailIdIgnoreCase(userEntity.getEmailId());
        if(existingUser != null)
        {
            modelAndView.addObject("message","This email already exists!");
            modelAndView.setViewName("error");
        }
        else
        {
            userRepository.save(userEntity);

            ConfirmationToken confirmationToken = new ConfirmationToken(userEntity);

            confirmationTokenRepository.save(confirmationToken);

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(userEntity.getEmailId());
            String email = userEntity.getEmailId();
            System.out.println("Email Address: " + email);
            mailMessage.setSubject("Complete Registration!");
            mailMessage.setText("To confirm your account, please click here : "
            +"http://localhost:8080/confirm-account?token="+confirmationToken.getConfirmationToken());

            emailService.sendEmail(mailMessage);

            modelAndView.addObject("emailId", userEntity.getEmailId());

            modelAndView.setViewName("successfulRegisteration");
        }

        return modelAndView;
    }
    

    @GetMapping("/check-email-verification/{email}")
    public ResponseEntity<?> checkEmailVerification(@PathVariable("email") String email) {
        UserEntity user = userRepository.findByEmailIdIgnoreCase(email);
        if (user != null && user.isEnabled()) {
            return ResponseEntity.ok().body("Email is verified.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email is not verified.");
        }
    }
    @RequestMapping(value="/confirm-account", method= {RequestMethod.GET, RequestMethod.POST})
    public String confirmUserAccount(ModelAndView modelAndView, @RequestParam("token")String confirmationToken)
    {
        ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);

        if(token != null)
        {
        	UserEntity user = userRepository.findByEmailIdIgnoreCase(token.getUserEntity().getEmailId());
            user.setEnabled(true);
            userRepository.save(user);
            return "redirect:http://localhost:4200/register";
        }
        else
        {
            modelAndView.addObject("message","The link is invalid or broken!");
           return "error";
        }

        
    }
}
