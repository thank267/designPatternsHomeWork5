package thank267.Velocity;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;
import org.apache.velocity.VelocityContext;

@Tags({"Velocity generator"})
@CapabilityDescription("Velocity generator")
public interface VelocityService extends ControllerService {

	String generate(VelocityContext context, String template) throws Exception;

}
