package org.mule.extension.internal;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;

import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.route.Chain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class CustomloggingOperations {

  private final Logger LOGGER = LoggerFactory.getLogger(CustomloggingOperations.class);

  /**
	 * Generate a transaction id if required, otherwise return the current
	 * transaction id. Creates a transactionProperties (as a LinkedHashMap<String, String>) containing the x-transaction-id and optionally
	 * any values from the specified headers (as a MultiMap).
	 */
	@MediaType(value = ANY, strict = false)
	@Alias("generateLog")
	public LinkedHashMap<String, String> setTransactionProperties(@Optional Map headers, @Optional @ParameterDsl(allowInlineDefinition=true) LinkedHashMap<String, String> customAttributes, ComponentLocation flow) {
		LinkedHashMap<String, String> transactionProperties = new LinkedHashMap<String, String>();

		addFlow(transactionProperties, flow);

    LinkedHashMap<String, String> tempMap = new LinkedHashMap<String, String>();

    LocalDateTime currentTime = LocalDateTime.now();
    tempMap.put("dateTimeStamp", currentTime.toString());

    tempMap.put("action", "Generated Log");

		if (headers != null) {
			if (headers.get("client_id") != null) {
				transactionProperties.put("client_id", (String) headers.get("client_id"));
			} else if (headers.get("x-client-id") != null) {
				transactionProperties.put("x-client-id", (String) headers.get("x-client-id"));
			}
      if (headers.get("x-correlation-id") != null) {
				transactionProperties.put("x-correlation-id", (String) headers.get("x-correlation-id"));
			} else if (headers.get("x_correlation_id") != null) {
				transactionProperties.put("x-correlation-id", (String) headers.get("x_correlation_id"));
			} else {
				transactionProperties.put("x-correlation-id", UUID.randomUUID().toString());
			}
			if (headers.get("x-transaction-id") != null) {
				transactionProperties.put("x-transaction-id", (String) headers.get("x-transaction-id"));
			} else if (headers.get("x_transaction_id") != null) {
				transactionProperties.put("x-transaction-id", (String) headers.get("x_transaction_id"));
			} else {
				transactionProperties.put("x-transaction-id", UUID.randomUUID().toString());
			}
		} else {
      transactionProperties.put("x-correlation-id", UUID.randomUUID().toString());
			transactionProperties.put("x-transaction-id", UUID.randomUUID().toString());
		}

    if (transactionProperties != null) {
			for (String item : transactionProperties.keySet()) {
				tempMap.put(item, transactionProperties.get(item));
			}
		}
		if (customAttributes != null) {
			for (String item : customAttributes.keySet()) {
				tempMap.put(item, customAttributes.get(item));
			}
		}
    StringBuilder sb = new StringBuilder();
		sb.append("");
		logMessage("INFO", sb.toString(), tempMap);

		return transactionProperties;
	}

  /**
	 * Scope for generating enter and exit message with elapsed time
	 * @param executionDescription
	 * @param transactionProperties
	 * @param flow
	 * @param operations
	 * @param callback
	 */
	public void executionTime(
			@Optional String executionDescription,
			@Optional(defaultValue="#[{}]") @ParameterDsl(allowInlineDefinition=false) LinkedHashMap<String, String> transactionProperties,
			ComponentLocation flow,
			Chain operations,
			CompletionCallback<Object, Object> callback) {

		long startTime = System.currentTimeMillis();

		LinkedHashMap<String, String> tempMap = new LinkedHashMap<String, String>();

    LocalDateTime enterTime = LocalDateTime.now();
    tempMap.put("dateTimeStamp", enterTime.toString());

    tempMap.put("action", "Entering " + executionDescription.toString());

		if (transactionProperties != null) {
			for (String item : transactionProperties.keySet()) {
				tempMap.put(item, transactionProperties.get(item));
			}
		}

		addFlow(tempMap, flow);

		StringBuilder sb = new StringBuilder();
		logMessage("INFO", sb.toString(), tempMap);

		operations.process(result -> {
      LocalDateTime exitTime = LocalDateTime.now();
      tempMap.put("dateTimeStamp", exitTime.toString());
      tempMap.put("action", "Exiting " + executionDescription.toString());
			long elapsedTime = System.currentTimeMillis() - startTime;
			tempMap.put("elapsedMS", Long.toString(elapsedTime));
			StringBuilder sbsuccess = new StringBuilder();
			logMessage("INFO", sbsuccess.toString(), tempMap);
			callback.success(result);
		}, (error, previous) -> {
      LocalDateTime exitTime = LocalDateTime.now();
      tempMap.put("dateTimeStamp", exitTime.toString());
			long elapsedTime = System.currentTimeMillis() - startTime;
			tempMap.put("elapsedMS", Long.toString(elapsedTime));
			StringBuilder sberror = new StringBuilder();
			sberror.append("Exit with error " + executionDescription).append(error.getMessage());
			logMessage("ERROR", sberror.toString(), tempMap);
			callback.error(error);
		});
	}

  /**
	 * Generate a log message of level INFO, WARN, ERROR or DEBUG.  All other levels result in no message generated.
	 *
	 * @param level
	 * @param msg
	 * @param transactionProperties
	 * @param flow
	 */
	public void log(@Optional(defaultValue="INFO") String level,
			String msg,
      @Optional @ParameterDsl(allowInlineDefinition=true) LinkedHashMap<String, String> newProperties,
			@Optional(defaultValue="#[{}]") @ParameterDsl(allowInlineDefinition=false) LinkedHashMap<String, String> transactionProperties,
			ComponentLocation flow) {

    LinkedHashMap<String, String> tempMap = new LinkedHashMap<String, String>();

    LocalDateTime currentTime = LocalDateTime.now();
    tempMap.put("dateTimeStamp", currentTime.toString());

    tempMap.put("Message", msg.toString());

    if (newProperties != null) {
      for (String item : newProperties.keySet()) {
				tempMap.put(item, newProperties.get(item));
			}
		}

		if (transactionProperties != null) {
			for (String item : transactionProperties.keySet()) {
				tempMap.put(item, transactionProperties.get(item));
			}
		}

		addFlow(tempMap, flow);

		logMessage(level.toUpperCase(), "", tempMap);
	}

  /*
	 * Add component location values to the transactionProperties
	 */
	private void addFlow(LinkedHashMap <String, String> transactionProperties, ComponentLocation flow) {
		if (flow != null) {
			transactionProperties.put("flowName", flow.getRootContainerName());
		} else {
			LOGGER.debug("Missing flow information");
		}
	}

  /*
	 * Write a log message
	*/
	private void logMessage(String level, String msg, LinkedHashMap<String, String> transactionProperties) {

		switch (level) {
		case ("INFO"):
			LOGGER.info(formatLogMsg(msg, transactionProperties));
			break;
		case ("DEBUG"):
			LOGGER.debug(formatLogMsg(msg, transactionProperties));
			break;
		case ("ERROR"):
			LOGGER.error(formatLogMsg(msg, transactionProperties));
			break;
		case ("WARN"):
			LOGGER.warn(formatLogMsg(msg, transactionProperties));
			break;
		default:
			//do nothing
		}
	}

  /*
  * Create the log message by adding the transactionProperties to the message as a JSON payload
  */
  private String formatLogMsg(String msg, LinkedHashMap<String, String> transactionProperties) {
		ObjectMapper mapper = new ObjectMapper();
		String payload = "";
		try {
			if (transactionProperties != null) {
				payload = mapper.writeValueAsString(transactionProperties);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		sb.append(msg).append(" ").append(payload);
		return sb.toString();
  }

}
