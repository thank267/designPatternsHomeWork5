package thank267.Velocity;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ExtProperties;

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ZookeeperResourceLoader extends ResourceLoader {

	private static final String BASE_ZK_PATH_DEFAULT = "/configs/application/templating";

	private static CuratorFramework curator;

	public ZookeeperResourceLoader(CuratorFramework curator) {

		ZookeeperResourceLoader.curator = curator;

	}

	@Override
	public void init(ExtProperties configuration) {

		try {

		} catch (Exception e) {
			throw new ProcessException(e);
		}

	}

	@Override
	public synchronized Reader getResourceReader(String name, String encoding) throws ResourceNotFoundException {

		String path = BASE_ZK_PATH_DEFAULT;

		if (StringUtils.isEmpty(name)) {
			throw new ResourceNotFoundException("DataSourceResourceLoader: Template name was empty or null");
		}

		if (StringUtils.equals(name, "velocimacros.vtl")) {

			throw new ResourceNotFoundException("velocimacros.vtl");
		}

		if (StringUtils.equals(name, "VM_global_library.vm")) {

			throw new ResourceNotFoundException("VM_global_library.vm");
		}

		path = path + "/renderers/" + name;

		Reader result = null;

		try {

			Optional.ofNullable(curator).orElseThrow(() -> new Exception(String.format("Получили пустой curator")));

			if (exists(curator, path) != null) {

				byte[] b = getData(curator, path);

				String fromZK = new String(b, StandardCharsets.UTF_8);

				result = new StringReader(fromZK);

			} else {

				throw new ResourceNotFoundException(String.format("No template name provided: %s", path));
			}

		} catch (Throwable e) {

			throw new ResourceNotFoundException(e.getMessage());

		}

		return result;
	}

	@Override
	public boolean isSourceModified(org.apache.velocity.runtime.resource.Resource resource) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getLastModified(org.apache.velocity.runtime.resource.Resource resource) {
		// TODO Auto-generated method stub
		return 0;
	}

	private Object exists(CuratorFramework client, String path) throws Exception {
		// this will create the given ZNode with the given data
		return client.checkExists().forPath(path);
	}

	private byte[] getData(CuratorFramework client, String path) throws Exception {
		// this will create the given ZNode with the given data
		return client.getData().forPath(path);
	}

}
