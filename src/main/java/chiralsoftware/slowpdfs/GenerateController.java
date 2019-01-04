package chiralsoftware.slowpdfs;

import java.util.Date;
import java.util.Map;
import static java.util.Map.of;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GenerateController {

    private static final Logger LOG = Logger.getLogger(GenerateController.class.getName());
    
    @Autowired
    public GenerateController(GenerateService generateService) {
        this.generateService = generateService;
    }
    
    private final GenerateService generateService;

    @PostMapping("/start")
    @ResponseBody
    public Map<String,String> start(@RequestParam(required = false) Integer number) throws Exception {
        LOG.info("Here is the start!");
        generateService.generate(number == null ? 100 : number);
        return of("message", "Started at: " + new Date());
    }
    
    @GetMapping("/")
    public String index() { 
        return "/index"; 
    }
    
}
