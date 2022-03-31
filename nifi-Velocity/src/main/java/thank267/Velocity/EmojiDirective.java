package thank267.Velocity;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.slf4j.Logger;
import thank267.NLP.EmojisService;

import java.io.IOException;
import java.io.Writer;

public class EmojiDirective extends Directive {

	private EmojisService emojisService;

	private Logger log;

	@Override
	public String getName() {
		return "emoji";
	}

	@Override
	public int getType() {
		return LINE;
	}

	@Override
	public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
		super.init(rs, context, node);
		emojisService = (EmojisService) rs.getProperty("emojis");
		this.log = rs.getLog();

	}

	@Override
	public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

		String type = null;

		String emoji = null;

		if (node.jjtGetChild(0) != null) {
			type = String.valueOf(node.jjtGetChild(0).value(context));
		}

		if (type == null || type.equals("positive")) {

			emoji = emojisService.getRandomPositive();

		} else if (type.equals("superscript two")) {

			emoji = emojisService.superscriptTwo();

		} else if (type.equals("bullet")) {

			emoji = emojisService.bullet();

		}

		writer.write(emoji);
		return true;
	}

}
