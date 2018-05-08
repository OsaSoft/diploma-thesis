package cz.cvut.fel.hernaosc.dp.msgr.core.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
class HealthController {
    @RequestMapping(method = RequestMethod.GET)
    def getHealth() {
        //TODO, possibly like this https://stackoverflow.com/questions/10097491/call-and-receive-output-from-python-script-in-java?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        [load: 0]
    }
}
