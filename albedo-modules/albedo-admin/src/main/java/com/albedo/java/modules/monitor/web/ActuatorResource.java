/*
 *  Copyright 2019-2020 somewhere
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.albedo.java.modules.monitor.web;

import com.albedo.java.common.core.util.Result;
import com.albedo.java.modules.monitor.domain.ReportSearchType;
import com.albedo.java.modules.monitor.service.MonitorService;
import com.albedo.java.modules.monitor.service.RedisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author somewhere
 * @date 2020-05-02
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "服务监控")
@RequestMapping("${application.admin-path}/actuator/")
public class ActuatorResource {

	private final MonitorService monitorService;

	private final RedisService redisService;

	@GetMapping("system")
	@ApiOperation("查询服务监控")
	public Result getServers() {
		return Result.buildOkData(monitorService.getServers());
	}

	/**
	 * Redis详细信息
	 *
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/redis/detail")
	public Result getRedisInfo() {
		return Result.buildOkData(redisService.getRedisInfo());
	}

	/**
	 * 获取redis key数量 for 报表
	 *
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/redis/keys-size-report")
	public Result getKeysSizeReport() {
		return Result.buildOkData(redisService.getMapForReport(ReportSearchType.KEY));
	}

	/**
	 * 获取redis 内存 for 报表
	 *
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/redis/memory-report")
	public Result memoryForReport() {
		return Result.buildOkData(redisService.getMapForReport(ReportSearchType.RAM));
	}

	/**
	 * 获取redis 全部信息 for 报表
	 *
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/redis/info-report")
	public Result infoForReport() {

		return Result.buildOkData(redisService.getMapForReport(ReportSearchType.INFO));

	}

}
