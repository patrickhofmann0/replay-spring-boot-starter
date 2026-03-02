package io.github.patrickhofmann0.replay_demo.replay_app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class DemoController {

    @GetMapping("/hello")
    @ResponseBody
    public String hello(@RequestParam(defaultValue = "World") String name) {
        return "Hello, " + name + "!";
    }

    @PostMapping("/echo")
    @ResponseBody
    public String echo(@RequestBody EchoRequest name) {
        return "Hello, " + name.name() + "!";
    }

    public record EchoRequest(String name) {
    }

}