package com.github.docker.replica.client.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.docker.replica.client.BaseServiceProvider;
import com.phonepe.platform.http.v2.common.HttpConfiguration;
import org.apache.curator.framework.CuratorFramework;

public class DockerCommandsProvider extends BaseServiceProvider {

    public DockerCommandsProvider(HttpConfiguration httpConfiguration,
                                  ObjectMapper mapper,
                                  CuratorFramework curatorFramework) {
        super(httpConfiguration, mapper, curatorFramework);
    }
}
