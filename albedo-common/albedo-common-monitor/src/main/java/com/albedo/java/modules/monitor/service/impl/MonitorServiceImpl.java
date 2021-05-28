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
package com.albedo.java.modules.monitor.service.impl;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import com.albedo.java.common.core.util.FileUtil;
import com.albedo.java.common.core.util.WebUtil;
import com.albedo.java.modules.monitor.service.MonitorService;
import com.albedo.java.modules.monitor.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.VirtualMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;
import oshi.util.Util;

import javax.annotation.Resource;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author somewhere
 * @date 2020-05-02
 */
@Service
@Slf4j
public class MonitorServiceImpl implements MonitorService {

	private final DecimalFormat df = new DecimalFormat("0.00");

	@Resource
	private RedisService redisService;

	@Override
	public Map<String, Object> getServers() {
		Map<String, Object> resultMap = new LinkedHashMap<>(8);
		try {
			SystemInfo si = new SystemInfo();
			OperatingSystem os = si.getOperatingSystem();
			HardwareAbstractionLayer hal = si.getHardware();
			// 系统信息
			resultMap.put("sys", getSystemInfo(os));
			// cpu 信息
			resultMap.put("cpu", getCpuInfo(hal.getProcessor()));
			// 内存信息
			resultMap.put("memory", getMemoryInfo(hal.getMemory()));
			// 交换区信息
			resultMap.put("swap", getSwapInfo(hal.getMemory()));
			// 磁盘
			resultMap.put("disk", getDiskInfo(os));
			// redis
			resultMap.put("redis", getRedisInfo());
			resultMap.put("time", DateUtil.format(new Date(), "HH:mm:ss"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultMap;
	}

	/**
	 * 获取redis信息
	 *
	 * @return
	 */
	private Map<String, Object> getRedisInfo() {
		Map<String, Object> redisInfo = new LinkedHashMap<>();
		redisInfo.put("keySize", redisService.getKeySize());
		redisInfo.put("usedMemory", redisService.getUsedMemory());
		return redisInfo;
	}

	/**
	 * 获取磁盘信息
	 *
	 * @return
	 */
	private Map<String, Object> getDiskInfo(OperatingSystem os) {
		Map<String, Object> diskInfo = new LinkedHashMap<>();
		FileSystem fileSystem = os.getFileSystem();
		List<OSFileStore> fsArray = fileSystem.getFileStores();
		for (OSFileStore fs : fsArray) {
			long available = fs.getUsableSpace();
			long total = fs.getTotalSpace();
			long used = total - available;
			diskInfo.put("total", total > 0 ? FileUtil.getSize(total) : "?");
			diskInfo.put("available", FileUtil.getSize(available));
			diskInfo.put("used", FileUtil.getSize(used));
			diskInfo.put("usageRate", df.format(used / (double) fs.getTotalSpace() * 100));
		}
		return diskInfo;
	}

	/**
	 * 获取交换区信息
	 *
	 * @param memory /
	 * @return
	 */
	private Map<String, Object> getSwapInfo(GlobalMemory memory) {
		Map<String, Object> swapInfo = new LinkedHashMap<>();
		VirtualMemory virtualMemory = memory.getVirtualMemory();
		long total = virtualMemory.getSwapTotal();
		long used = virtualMemory.getSwapUsed();
		swapInfo.put("total", FormatUtil.formatBytes(total));
		swapInfo.put("used", FormatUtil.formatBytes(used));
		swapInfo.put("available", FormatUtil.formatBytes(total - used));
		if (used == 0) {
			swapInfo.put("usageRate", 0);
		} else {
			swapInfo.put("usageRate", df.format(used / (double) total * 100));
		}
		return swapInfo;

	}

	/**
	 * 获取内存信息
	 *
	 * @param memory /
	 * @return
	 */
	private Map<String, Object> getMemoryInfo(GlobalMemory memory) {
		Map<String, Object> memoryInfo = new LinkedHashMap<>();
		memoryInfo.put("total", FormatUtil.formatBytes(memory.getTotal()));
		memoryInfo.put("available", FormatUtil.formatBytes(memory.getAvailable()));
		memoryInfo.put("used", FormatUtil.formatBytes(memory.getTotal() - memory.getAvailable()));
		memoryInfo.put("usageRate",
			df.format((memory.getTotal() - memory.getAvailable()) / (double) memory.getTotal() * 100));
		return memoryInfo;
	}

	/**
	 * 获取Cpu相关信息
	 *
	 * @param processor /
	 * @return
	 */
	private Map<String, Object> getCpuInfo(CentralProcessor processor) {
		Map<String, Object> cpuInfo = new LinkedHashMap<>();
		cpuInfo.put("name", processor.getProcessorIdentifier().getName());
		cpuInfo.put("package", processor.getPhysicalPackageCount() + "个物理CPU");
		cpuInfo.put("core", processor.getPhysicalProcessorCount() + "个物理核心");
		cpuInfo.put("coreNumber", processor.getPhysicalProcessorCount());
		cpuInfo.put("logic", processor.getLogicalProcessorCount() + "个逻辑CPU");
		// CPU信息
		long[] prevTicks = processor.getSystemCpuLoadTicks();
		// 等待1秒...
		Util.sleep(1000);
		long[] ticks = processor.getSystemCpuLoadTicks();
		long user = ticks[CentralProcessor.TickType.USER.getIndex()]
			- prevTicks[CentralProcessor.TickType.USER.getIndex()];
		long nice = ticks[CentralProcessor.TickType.NICE.getIndex()]
			- prevTicks[CentralProcessor.TickType.NICE.getIndex()];
		long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()]
			- prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
		long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()]
			- prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
		long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()]
			- prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
		long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()]
			- prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
		long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()]
			- prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
		long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()]
			- prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
		long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;
		cpuInfo.put("used", df.format(100d * user / totalCpu + 100d * sys / totalCpu));
		cpuInfo.put("idle", df.format(100d * idle / totalCpu));
		return cpuInfo;
	}

	/**
	 * 获取系统相关信息,系统、运行天数、系统IP
	 *
	 * @param os /
	 * @return
	 */
	private Map<String, Object> getSystemInfo(OperatingSystem os) {
		Map<String, Object> systemInfo = new LinkedHashMap<>();
		// jvm 运行时间
		long time = ManagementFactory.getRuntimeMXBean().getStartTime();
		Date date = new Date(time);
		// 计算项目运行时间
		String formatBetween = DateUtil.formatBetween(date, new Date(), BetweenFormatter.Level.HOUR);
		// 系统信息
		systemInfo.put("os", os.toString());
		systemInfo.put("day", formatBetween);
		systemInfo.put("ip", WebUtil.getHostIp());
		return systemInfo;
	}

}
