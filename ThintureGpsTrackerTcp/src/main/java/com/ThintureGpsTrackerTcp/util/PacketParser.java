package com.ThintureGpsTrackerTcp.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ThintureGpsTrackerTcp.handler.GPSWebSocketHandler;
import com.ThintureGpsTrackerTcp.model.DeviceHistory;
import com.ThintureGpsTrackerTcp.model.LastKnownLocationHttps;
import com.ThintureGpsTrackerTcp.repository.DeviceHistoryRepository;
import com.ThintureGpsTrackerTcp.repository.LastKnownLocationHttpsRepository;
import com.ThintureGpsTrackerTcp.service.DeviceHistoryService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
@Component
public class PacketParser {

	private static final Logger logger = LoggerFactory.getLogger(PacketParser.class);
	private final GPSWebSocketHandler gpsWebSocketHandler;
	 private final DeviceHistoryRepository deviceHistoryRepository;

	    @Autowired
	    private LastKnownLocationHttpsRepository lastKnownLocationHttpsRepository;
	    @Autowired
	    private DeviceHistoryService deviceHistoryService;
	 @Autowired
	 public PacketParser(GPSWebSocketHandler gpsWebSocketHandler, DeviceHistoryRepository deviceHistoryRepository) {
	     this.gpsWebSocketHandler = gpsWebSocketHandler;
	     this.deviceHistoryRepository = deviceHistoryRepository;
	 }

	
	// Default constructor for compatibility (not ideal for production, primarily
	// for testing)
	public PacketParser() {
		this.gpsWebSocketHandler = new GPSWebSocketHandler();
		this.deviceHistoryRepository = null;
	}

	// Base date to calculate timestamps relative to the Unix epoch (Jan 1, 2000)
	private static final long BASE_DATE_SECONDS = 946684800;

	/**
	 * Parses and broadcasts GPS parameters including latitude, longitude, and
	 * timestamp. This method is responsible for extracting the provided hex data,
	 * converting it into usable formats, and broadcasting it via WebSocket.
	 *
	 * @param deviceID     Unique identifier for the GPS device.
	 * @param latitudeHex  Hexadecimal string representing latitude.
	 * @param longitudeHex Hexadecimal string representing longitude.
	 * @param timestampHex Hexadecimal string representing the timestamp.
	 */
	public void extractAndBroadcastParameters(String deviceID, String latitudeHex, String longitudeHex, byte[] buffer) {
	    Double latitude = null; // Parsed latitude in decimal degrees
	    Double longitude = null; // Parsed longitude in decimal degrees
	    Long timestamp = null; // Parsed timestamp in seconds
	    Integer speed = null; // Parsed speed in km/h
	    String ignitionState = null; // Ignition state (ON/OFF)
	    logger.debug("\u001B[36mStarting extraction and broadcast of parameters for device {}\u001B[0m", deviceID);

	    // Convert latitude hex string to decimal degrees
	    try {
	        latitude = convertHexToDegrees(latitudeHex);
	        logger.info("\u001B[34mDevice {} - Parsed Latitude (Decimal Degrees): {} degrees\u001B[0m", deviceID, latitude);
	    } catch (IllegalArgumentException e) {
	        logger.error("\u001B[31mDevice {} - Latitude conversion error: {}\u001B[0m", deviceID, e.getMessage());
	    }

	    // Convert longitude hex string to decimal degrees
	    try {
	        longitude = convertHexToDegrees(longitudeHex);
	        logger.info("\u001B[34mDevice {} - Parsed Longitude (Decimal Degrees): {} degrees\u001B[0m", deviceID,
	                longitude);
	    } catch (IllegalArgumentException e) {
	        logger.error("\u001B[31mDevice {} - Longitude conversion error: {}\u001B[0m", deviceID, e.getMessage());
	    }

	    // Extract and parse timestamp
	    try {
	        String timestampHex = extractHex(buffer, 0x04); // Assuming 0x04 is the parameter ID for timestamp
	        if (timestampHex != null) {
	            byte[] timestampBytes = parseHexStringToByteArray(timestampHex);
	            timestamp = convertHexToTimestamp(timestampBytes);
	            String formattedTimestamp = formatTimestamp(timestamp);
	            logger.info("\u001B[36mDevice {} - Extracted and Formatted Timestamp: {}\u001B[0m", deviceID,
	                    formattedTimestamp);
	        } else {
	            logger.warn("\u001B[33mDevice {} - Timestamp Hex data missing in buffer.\u001B[0m", deviceID);
	        }
	    } catch (IllegalArgumentException e) {
	        logger.error("\u001B[31mDevice {} - Timestamp parsing error: {}\u001B[0m", deviceID, e.getMessage());
	    }

	    if (timestamp == null) {
	        logger.warn("\u001B[33mDevice {} - No valid timestamp found in the buffer.\u001B[0m", deviceID);
	    }

	    
	    
	    
	    
	    // Extract speed from the buffer
	    if (buffer != null) {
	        for (int i = 0; i < buffer.length - 4; i++) {
	            if (buffer[i] == (byte) 0x08) { // Check for speed marker 0x08
	                if ((i + 3) < buffer.length && buffer[i + 3] == (byte) 0x09) { // Check for end marker 0x09
	                    int speedLSB = buffer[i + 1] & 0xFF; // Least Significant Byte
	                    int speedMSB = buffer[i + 2] & 0xFF; // Most Significant Byte
	                    speed = (speedMSB << 8) | speedLSB; // Combine LSB and MSB to get the speed
	                    logger.info("\u001B[32mDevice {} - Extracted Speed: {} km/h\u001B[0m", deviceID, speed);
	                    break; // Stop after finding the first valid speed
	                }
	            }
	        }
	    }

	    if (speed == null) {
	        logger.warn("\u001B[33mDevice {} - No valid speed found in the buffer.\u001B[0m", deviceID);
	    }

	 // Extract ignition status using extractAndLogSystemFlags
	    Map<String, Boolean> systemFlags = extractAndLogSystemFlags(buffer, deviceID);
	    boolean isIgnitionOn = systemFlags.getOrDefault("ACC Status (ON/OFF)", false);
	   // logger.info("\u001B[1;36mDevice {}\u001B[0m - Ignition (System Flags) is {}.", deviceID,
	     //       isIgnitionOn ? "\u001B[1;32mON\u001B[0m" : "\u001B[1;31mOFF\u001B[0m");

	    // Extract ignition state using the processStateChange method
	    ignitionState = processStateChange(buffer, deviceID);

	    // Log the ignition state
	    if (ignitionState != null) {
	        logger.info("\u001B[36mDevice {} - Ignition (ProcessStateChange) is: {}\u001B[0m", deviceID, ignitionState);
	    } else {
	        logger.warn("\u001B[33mDevice {} - Ignition state could not be determined.\u001B[0m", deviceID);
	    }

	    // Broadcast data if all values are valid
	    if (latitude != null && longitude != null && timestamp != null && speed != null) {
	        try {
	            gpsWebSocketHandler.broadcastLocationWithIgnition(deviceID, latitude, longitude, timestamp, speed, isIgnitionOn, ignitionState);
	            logger.debug(
	                    "\u001B[36mDevice {} - Broadcasted latitude, longitude, timestamp, speed, and ignition state (System Flags) successfully.\u001B[0m",
	                    deviceID);
	        } catch (Exception e) {
	            logger.error(
	                    "\u001B[31mDevice {} - Error broadcasting location, timestamp, speed, and ignition state via WebSocket: {}\u001B[0m",
	                    deviceID, e.getMessage());
	        }
	    } else {
	        logger.warn(
	                "\u001B[33mDevice {} - Invalid latitude, longitude, timestamp, or speed; broadcasting skipped.\u001B[0m",
	                deviceID);
	    }
	}

	
	
	public void extractAndSaveParameters(String deviceID, String latitudeHex, String longitudeHex, byte[] buffer) {
	    logger.debug("Starting extraction and saving of parameters for device ID: {}", deviceID);

	    Double latitude = null;
	    Double longitude = null;
	    Long timestamp = null;
	    Integer speed = null;
	    String ignitionState = null;

	    // Latitude Extraction and Conversion
	    try {
	        logger.debug("Device {}: Attempting to convert latitude from hex: {}", deviceID, latitudeHex);
	        latitude = convertHexToDegrees(latitudeHex);
	        logger.info("Device {}: Parsed Latitude: {} degrees", deviceID, latitude);
	    } catch (IllegalArgumentException e) {
	        logger.error("Device {}: Latitude conversion failed: {}", deviceID, e.getMessage(), e);
	    }

	    // Longitude Extraction and Conversion
	    try {
	        logger.debug("Device {}: Attempting to convert longitude from hex: {}", deviceID, longitudeHex);
	        longitude = convertHexToDegrees(longitudeHex);
	        logger.info("Device {}: Parsed Longitude: {} degrees", deviceID, longitude);
	    } catch (IllegalArgumentException e) {
	        logger.error("Device {}: Longitude conversion failed: {}", deviceID, e.getMessage(), e);
	    }

	    // Timestamp Extraction
	    try {
	        logger.debug("Device {}: Attempting to extract timestamp from buffer", deviceID);
	        String timestampHex = extractHex(buffer, 0x04);
	        if (timestampHex != null) {
	            byte[] timestampBytes = parseHexStringToByteArray(timestampHex);
	            timestamp = convertHexToTimestamp(timestampBytes);
	            logger.info("Device {}: Extracted and Formatted Timestamp: {}", deviceID, timestamp);
	        } else {
	            logger.warn("Device {}: Timestamp hex missing in buffer", deviceID);
	        }
	    } catch (Exception e) {
	        logger.error("Device {}: Error extracting timestamp: {}", deviceID, e.getMessage(), e);
	    }

	    // Speed Extraction
	    logger.debug("Device {}: Attempting to extract speed from buffer", deviceID);
	    speed = extractSpeedFromBuffer(buffer, deviceID);

	    // Ignition State Extraction
	    logger.debug("Device {}: Attempting to determine ignition state from buffer", deviceID);
	    ignitionState = processStateChange(buffer, deviceID);

	    // Saving Data to Database
	    logger.debug("Device {}: Preparing to save data to database", deviceID);
	    saveToDatabase(deviceID, latitude, longitude, timestamp, speed, ignitionState);
	    logger.debug("Device {}: Data save process completed", deviceID);
	}

	private Integer extractSpeedFromBuffer(byte[] buffer, String deviceID) {
	    try {
	        logger.debug("Device {}: Starting speed extraction from buffer", deviceID);
	        if (buffer != null) {
	            for (int i = 0; i < buffer.length - 4; i++) {
	                if (buffer[i] == (byte) 0x08 && (i + 3) < buffer.length && buffer[i + 3] == (byte) 0x09) {
	                    int speedLSB = buffer[i + 1] & 0xFF;
	                    int speedMSB = buffer[i + 2] & 0xFF;
	                    int speed = (speedMSB << 8) | speedLSB;
	                    logger.info("Device {}: Extracted Speed: {} km/h", deviceID, speed);
	                    return speed;
	                }
	            }
	        }
	    } catch (Exception e) {
	        logger.error("Device {}: Error extracting speed: {}", deviceID, e.getMessage(), e);
	    }
	    logger.warn("Device {}: Speed extraction failed, returning 0", deviceID);
	    return 0; // Default speed
	}

	private void saveToDatabase(String deviceID, Double latitude, Double longitude, Long timestamp, Integer speed, String ignitionState) {
	    try {
	        logger.debug("Preparing to save data: DeviceID: {}, Latitude: {}, Longitude: {}, Timestamp (Epoch): {}, Speed: {}, IgnitionState: {}",
	                deviceID, latitude, longitude, timestamp, speed, ignitionState);

	        // Validate required fields
	        if (deviceID == null || latitude == null || longitude == null || timestamp == null) {
	            logger.warn("Missing required fields for saving: DeviceID: {}, Timestamp: {}", deviceID, timestamp);
	            return;
	        }

	        // Log raw timestamp
	        logger.debug("Raw timestamp (Epoch seconds): {}", timestamp);

	        // Convert the timestamp to LocalDateTime (UTC)
	        ZonedDateTime utcDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("UTC"));
	        logger.debug("Converted timestamp to UTC: {}", utcDateTime);

	        // Convert to Asia/Kolkata time zone
	        ZonedDateTime kolkataDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
	        LocalDateTime localDateTime = kolkataDateTime.toLocalDateTime();
	        logger.debug("Converted UTC timestamp to Asia/Kolkata LocalDateTime: {}", localDateTime);

	        // Handle Ignition State: "OFF"
	        if ("OFF".equalsIgnoreCase(ignitionState)) {
	            logger.info("Device {}: Ignition is OFF. Updating last known location in last_known_location_https table.", deviceID);

	            try {
	                // Check if an existing record exists in the last_known_location_https table
	                Optional<LastKnownLocationHttps> existingLocationOpt = lastKnownLocationHttpsRepository.findByDeviceId(deviceID);
	                LastKnownLocationHttps location;

	                if (existingLocationOpt.isPresent()) {
	                    location = existingLocationOpt.get();
	                    logger.info("Existing record found for DeviceID: {}. Updating last known location.", deviceID);
	                } else {
	                    location = new LastKnownLocationHttps();
	                    location.setDeviceId(deviceID);
	                    logger.info("No existing record found for DeviceID: {}. Creating a new entry in last_known_location_https.", deviceID);
	                }

	                // Update the location details
	                location.setLatitude(latitude);
	                location.setLongitude(longitude);
	                location.setLastUpdated(localDateTime);

	                // Save the record
	                lastKnownLocationHttpsRepository.save(location);
	                logger.info("Last known location updated/inserted successfully for DeviceID: {}", deviceID);
	            } catch (Exception e) {
	                logger.error("Error updating last known location for DeviceID {}: {}", deviceID, e.getMessage(), e);
	            }
	        }

	        // Save to DeviceHistory regardless of the ignition state
	        DeviceHistory history = new DeviceHistory();
	        history.setDeviceId(deviceID);
	        history.setLatitude(latitude);
	        history.setLongitude(longitude);
	        history.setTimestamp(localDateTime); // Store as LocalDateTime
	        history.setSpeed(speed);
	        history.setIgnitionState(ignitionState);

	        // Log the final history object
	        logger.debug("Final DeviceHistory object to be saved: {}", history);

	        // Save to database
	        deviceHistoryService.saveDeviceHistory(history); // Delegating to service
	        logger.info("DeviceHistory saved successfully for DeviceID: {}", history.getDeviceId());
	    } catch (Exception e) {
	        logger.error("Error saving to database for DeviceID {}: {}", deviceID, e.getMessage(), e);
	    }
	}



	
	
	/**
	 * Converts a hexadecimal string to decimal degrees. The input string is
	 * expected to contain three or four space-separated hex values. The values are
	 * combined into a single decimal value and scaled to represent degrees.
	 * 
	 * @param hexData Hexadecimal string (e.g., "0xA9 0x7F 0xC7 0x00")
	 * @return Decimal degree representation
	 * @throws IllegalArgumentException if the hex data format is incorrect
	 */

	public static double convertHexToDegrees(String hexData) {
		String[] hexValues = hexData.trim().split("\\s+");
		if (hexValues.length != 3 && hexValues.length != 4) {
			throw new IllegalArgumentException("Incorrect number of hex values for conversion.");
		}

		long decimalValue = 0;
		for (int i = hexValues.length - 1; i >= 0; i--) {
			String hex = hexValues[i].startsWith("0x") ? hexValues[i].substring(2) : hexValues[i];
			decimalValue = (decimalValue << 8) | Integer.parseInt(hex, 16);
		}

		if (hexValues.length == 3 && decimalValue >= 0x800000L) {
			decimalValue -= 0x1000000L;
		}

		double degrees = decimalValue / 1_000_000.0;
		logger.debug("\u001B[34mConverted hex {} to degrees: {}\u001B[0m", hexData, degrees);

		return degrees;
	}

	/**
	 * Parses a space-separated hexadecimal string into a byte array. Each hex value
	 * is converted into a single byte.
	 * 
	 * @param hexString the hexadecimal string to be converted
	 * @return the byte array representing the hexadecimal values
	 */

	public byte[] parseHexStringToByteArray(String hexString) {
		logger.debug("\u001B[36mParsing hex string to byte array: {}\u001B[0m", hexString);

		String[] hexValues = hexString.trim().split("\\s+");
		byte[] byteArray = new byte[hexValues.length];

		for (int i = 0; i < hexValues.length; i++) {
			String hex = hexValues[i].startsWith("0x") ? hexValues[i].substring(2) : hexValues[i];
			byteArray[i] = (byte) Integer.parseInt(hex, 16);
		}

		logger.debug("Converted hex string '{}' to byte array: {}", hexString, bytesToHex(byteArray));
		return byteArray;
	}

	/**
	 * Converts a 4-byte hexadecimal array to a timestamp in seconds. Handles the
	 * conversion of a byte array by reversing the byte order and interpreting it as
	 * an unsigned integer.
	 * 
	 * @param hexData the hexadecimal byte array
	 * @return the timestamp in seconds since the base date (2000-01-01 00:00:00
	 *         UTC)
	 * @throws IllegalArgumentException if the hex data is not 4 bytes
	 */

	public long convertHexToTimestamp(byte[] hexData) {
		if (hexData == null || hexData.length != 4) {
			throw new IllegalArgumentException("Hex data must be exactly 4 bytes.");
		}

		logger.debug("Converting hex data to timestamp: {}", bytesToHex(hexData));
		byte[] reversedHex = reverseArray(hexData);

		ByteBuffer buffer = ByteBuffer.wrap(reversedHex);
		long timestampSeconds = buffer.getInt() & 0xFFFFFFFFL; // Handle as unsigned
		logger.info("Converted hex data '{}' to timestamp (seconds): {}", bytesToHex(hexData), timestampSeconds);
		return timestampSeconds;
	}

	/**
	 * Formats a timestamp (in seconds since the base date) to a human-readable date
	 * string. Converts the base-date-relative timestamp into an absolute date-time
	 * format.
	 * 
	 * @param timestampSeconds the timestamp in seconds
	 * @return the formatted date string in IST timezone
	 */

	public String formatTimestamp(long timestampSeconds) {
		long totalSeconds = BASE_DATE_SECONDS + timestampSeconds;
		Instant instant = Instant.ofEpochSecond(totalSeconds);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z")
				.withZone(ZoneId.of("Asia/Kolkata"));

		String formattedTimestamp = formatter.format(instant);
		logger.info("Formatted timestamp (seconds: {}) to date: {}", totalSeconds, formattedTimestamp);
		return formattedTimestamp;
	}

	/**
	 * Extracts a 4-byte hexadecimal value from a buffer at a given parameter ID
	 * position. The method locates the parameter ID in the buffer, validates its
	 * location, and retrieves the subsequent 4 bytes as a hex string.
	 * 
	 * @param buffer      the byte buffer containing data
	 * @param parameterId the parameter ID to locate
	 * @return the extracted 4-byte hexadecimal string or null if not found
	 */

	public String extractHex(byte[] buffer, int parameterId) {
		for (int i = 0; i < buffer.length - 5; i++) {
			// Look for the first occurrence of `parameterId` (0x04)
			if (buffer[i] == (byte) parameterId) {
				// Ensure there are at least 5 more bytes (4 bytes + ending 0x0C)
				if ((i + 5) < buffer.length && buffer[i + 5] == (byte) 0x0C) {
					// Extract the next 4 bytes
					StringBuilder hexBuilder = new StringBuilder();
					for (int j = i + 1; j < i + 5; j++) {
						hexBuilder.append(String.format("0x%02X ", buffer[j]));
					}
					return hexBuilder.toString().trim(); // Return the valid hex string
				}
			}
		}
		return null; // Return null if no valid sequence is found
	}

	/**
	 * Reverses the order of the given byte array. This is typically used when
	 * interpreting data with little-endian encoding.
	 * 
	 * @param array the byte array to reverse
	 * @return a new byte array with reversed order
	 */

	private byte[] reverseArray(byte[] array) {
		byte[] reversed = new byte[array.length];
		for (int i = 0; i < array.length; i++) {
			reversed[i] = array[array.length - 1 - i];
		}
		logger.debug("Reversed byte array: Original={}, Reversed={}", bytesToHex(array), bytesToHex(reversed));
		return reversed;
	}

	/**
	 * Finds the index of the first occurrence of a marker in the byte buffer.
	 * Searches the buffer starting at a given index for the specified byte marker.
	 * 
	 * @param buffer     the byte buffer to search
	 * @param startIndex the starting index for the search
	 * @param marker     the marker byte to find
	 * @return the index of the marker or -1 if not found
	 */

	private int findMarkerIndex(byte[] buffer, int startIndex, int marker) {
		for (int i = startIndex; i < buffer.length; i++) {
			if (buffer[i] == (byte) marker) {
				logger.debug("Found marker {} at index {}", marker, i);
				return i;
			}
		}
		logger.warn("Marker {} not found starting from index {}", marker, startIndex);
		return -1;
	}

	/**
	 * Extracts speed values from the buffer, validating blocks that start with
	 * `0x08` and end with `0x09`. Each valid block contains 2 bytes representing
	 * the speed value in little-endian format. Logs the hex data and speed in km/h
	 * for the first valid occurrence.
	 * 
	 * @param buffer   The byte array to parse
	 * @param deviceID The unique identifier of the device sending the data
	 * @return A list of valid speed values extracted from the buffer
	 */

	public List<Integer> extractAndLogSpeeds(byte[] buffer, String deviceID) {
		List<Integer> speeds = new ArrayList<>();
		boolean loggedOnce = false; // Flag to ensure only one log per valid speed

		for (int i = 0; i < buffer.length - 4; i++) {
			// Look for the 0x08 marker
			if (buffer[i] == (byte) 0x08) {
				// Ensure there are at least 2 bytes for speed and ends with 0x09
				if ((i + 3) < buffer.length && buffer[i + 3] == (byte) 0x09) {
					// Build the hex string for logging
					StringBuilder hexBuilder = new StringBuilder();
					for (int j = i + 1; j < i + 3; j++) {
						hexBuilder.append(String.format("0x%02X ", buffer[j]));
					}
					String speedHex = hexBuilder.toString().trim();

					// Convert the extracted 2 bytes into a speed value (little-endian)
					int speedLSB = buffer[i + 1] & 0xFF; // LSB
					int speedMSB = buffer[i + 2] & 0xFF; // MSB
					int speed = (speedMSB << 8) | speedLSB; // Combine bytes in little-endian

					speeds.add(speed);

					// Log only once per valid speed
					if (!loggedOnce) {
						// logger.info("Device {} - Converted hex data for speed: {} ({} km/h)",
						// deviceID, speedHex, speed);
						logger.info("\u001B[32mDevice {} - Converted hex data for speed: {} ({} km/h)\u001B[0m",
								deviceID, speedHex, speed);

						loggedOnce = true;
					}
				}
			}
		}

		return speeds;
	}

	/**
	 * Converts a byte array to a hexadecimal string. Useful for logging or
	 * debugging raw data in a readable format.
	 * 
	 * @param bytes the byte array to convert
	 * @return the hexadecimal string representation
	 */

	public Map<String, Boolean> extractAndLogSystemFlags(byte[] buffer, String deviceID) {
	    Map<String, Boolean> systemFlags = new LinkedHashMap<>();
	    boolean loggedOnce = false; // Flag to ensure only one log per valid system flag

	    if (buffer == null || buffer.length < 5) {
	    	//    logger.warn("Device {} - Buffer is null or too short to contain system flags.", deviceID);
	        return systemFlags;
	    }

	    for (int i = 0; i <= buffer.length - 5; i++) { // Ensure we don't exceed buffer length
	        // Check for the 0x1C marker for the system flag
	        if (buffer[i] == (byte) 0x1C) {
	            if ((i + 4) < buffer.length) {
	                // Extract and log hex representation
	                StringBuilder hexBuilder = new StringBuilder();
	                for (int j = i + 1; j < i + 5; j++) {
	                    hexBuilder.append(String.format("0x%02X ", buffer[j]));
	                }
	                String systemFlagHex = hexBuilder.toString().trim();

	                // Convert the 4-byte system flag into an integer
	                int systemFlag = ByteBuffer.wrap(buffer, i + 1, 4).getInt();

	                // Decode individual flags
	                systemFlags.put("Modify EEP2 Parameter", (systemFlag & (1 << 0)) != 0);
	                systemFlags.put("ACC Status (ON/OFF)", (systemFlag & (1 << 1)) != 0);
	                systemFlags.put("Anti-theft Status", (systemFlag & (1 << 2)) != 0);
	                systemFlags.put("Vibration Flag", (systemFlag & (1 << 3)) != 0);
	                systemFlags.put("Moving Flag", (systemFlag & (1 << 4)) != 0);
	                systemFlags.put("External Power Supply", (systemFlag & (1 << 5)) != 0);
	                systemFlags.put("Charging Status", (systemFlag & (1 << 6)) != 0);
	                systemFlags.put("Sleep Mode Enabled", (systemFlag & (1 << 7)) != 0);
	                systemFlags.put("FMS Connected", (systemFlag & (1 << 8)) != 0);
	                systemFlags.put("FMS Function Enabled", (systemFlag & (1 << 9)) != 0);

	                // Check if ignition (ACC) is ON
	                boolean isIgnitionOn = systemFlags.getOrDefault("ACC Status (ON/OFF)", false);

	                // Log details
	                if (!loggedOnce) {
	                	//            logger.info("Device {} - Extracted System Flag Hex: {}", deviceID, systemFlagHex);
	                    //              logger.info("Device {} - Decoded System Flags: {}", deviceID, systemFlags);
	                    //               logger.info("\u001B[36mDevice {}\u001B[0m - Ignition (ACC) Status: \u001B[33m{}\u001B[0m",
	                	//                deviceID, isIgnitionOn ? "\u001B[32mON\u001B[0m" : "\u001B[31mOFF\u001B[0m");
	                            //
	                    loggedOnce = true;
	                }
	            } else {
	            //    logger.warn("Device {} - Insufficient bytes for system flag after marker 0x1C.", deviceID);
	            }
	        }
	    }

	    if (systemFlags.isEmpty()) {
	    	//    logger.warn("Device {} - No valid system flag data found in the packet.", deviceID);
	    }

	    return systemFlags;
	}


// new two parameter 
	/**
	 * Extracts Mileage and Run Time from the buffer.
	 *
	 * @param buffer   The byte array containing data.
	 * @param deviceID The unique identifier of the device.
	 * @return A map containing Mileage (in meters) and Run Time (in seconds).
	 */
	/**
	 * Extracts Mileage and Run Time from the buffer.
	 *
	 * @param buffer   The byte array containing data.
	 * @param deviceID The unique identifier of the device.
	 * @return A map containing Mileage (in meters) and Run Time (in seconds).
	 */
	public Map<String, Object> extractMileageAndRunTime(byte[] buffer, String deviceID) {
		Map<String, Object> extractedParams = new HashMap<>();

		// Extract Mileage (Parameter ID: 0x0C, ends with 0x0D)
		for (int i = 0; i < buffer.length - 5; i++) {
			if (buffer[i] == (byte) 0x0C) {
				if ((i + 5) < buffer.length && buffer[i + 5] == (byte) 0x0D) {
					byte[] mileageBytes = Arrays.copyOfRange(buffer, i + 1, i + 5); // Next 4 bytes
					String mileageHex = bytesToHex(mileageBytes);
					long mileage = ByteBuffer.wrap(mileageBytes).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFFFFFFL; // Unsigned
																														// DWORD
					double mileageKm = mileage / 1000.0; // Convert meters to kilometers

					//			logger.info("\u001B[36mDevice {} - Extracted System Mileage Hex: {}\u001B[0m", deviceID,
					//		mileageHex);
					//				logger.info("\u001B[32mDevice {} - Extracted Mileage: {} meters ({} kilometers)\u001B[0m", deviceID,
					//	mileage, mileageKm);

					extractedParams.put("MileageMeters", mileage);
					extractedParams.put("MileageKilometers", mileageKm);
					break; // Stop after finding the first Mileage parameter
				}
			}
		}

		// Extract Run Time (Parameter ID: 0x0D, ends with 0x1C)
		for (int i = 0; i < buffer.length - 5; i++) {
			if (buffer[i] == (byte) 0x0D) {
				if ((i + 5) < buffer.length && buffer[i + 5] == (byte) 0x1C) {
					byte[] runTimeBytes = Arrays.copyOfRange(buffer, i + 1, i + 5); // Next 4 bytes
					String runTimeHex = bytesToHex(runTimeBytes);

					// Convert to seconds
					long runTimeSeconds = ByteBuffer.wrap(runTimeBytes).order(ByteOrder.LITTLE_ENDIAN).getInt()
							& 0xFFFFFFFFL; // Unsigned DWORD

					// Calculate run time as hours, minutes, and seconds
					long hours = runTimeSeconds / 3600;
					long minutes = (runTimeSeconds % 3600) / 60;
					long seconds = runTimeSeconds % 60;

					// Format the run time as HH:mm:ss
					String runTimeFormattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

					//				logger.info("Device {} - Extracted System Run Time Hex: {}", deviceID, runTimeHex);
					//			logger.info("Device {} - Extracted Run Time: {} seconds", deviceID, runTimeSeconds);
					//				logger.info("Device {} - Run Time formatted as time: {}", deviceID, runTimeFormattedTime);

					extractedParams.put("RunTimeSeconds", runTimeSeconds);
					extractedParams.put("RunTimeFormattedTime", runTimeFormattedTime);
					break; // Stop after finding the first Run Time parameter
				}
			}
		}

		return extractedParams;
	}

	public Double extractAltitude(byte[] buffer, String deviceID) {
		Double altitude = null;

		// Extract Altitude (Starts with 0x0B, 2 bytes, ends with 0x16)
		for (int i = 0; i < buffer.length - 3; i++) {
			if (buffer[i] == (byte) 0x0B) { // Check for Altitude Parameter ID
				if ((i + 3) < buffer.length && buffer[i + 3] == (byte) 0x16) { // Check for end marker 0x16
					byte[] altitudeBytes = Arrays.copyOfRange(buffer, i + 1, i + 3); // Next 2 bytes
					String altitudeHex = bytesToHex(altitudeBytes);
					short altitudeValue = ByteBuffer.wrap(altitudeBytes).order(ByteOrder.LITTLE_ENDIAN).getShort(); // Signed
																													// SINT16

					//			logger.info("Device {} - Extracted Altitude Hex: {}", deviceID, altitudeHex);
					//			logger.info("Device {} - Calculated Altitude: {} meters", deviceID, altitudeValue);

					altitude = (double) altitudeValue;
					break; // Stop after finding the first Altitude parameter
				}
			}
		}

		if (altitude == null) {
			//	logger.warn("Device {} - Altitude data not found or validation failed.", deviceID);
		}

		return altitude;
	}

	// battery AD1, AD4, AD5 parameters

	public Map<String, String> extractAnalogParameters(byte[] buffer, String deviceID) {
		Map<String, String> extractedParams = new HashMap<>();
		Set<String> loggedParams = new HashSet<>(); // To prevent duplicate logs

		// Extract AD1 (Parameter ID: 0x16)
		for (int i = 0; i < buffer.length - 3; i++) {
			if (buffer[i] == (byte) 0x16 && !loggedParams.contains("AD1")) {
				byte[] ad1Bytes = Arrays.copyOfRange(buffer, i + 1, i + 3);
				String ad1Hex = bytesToHex(ad1Bytes);
				int ad1Value = ByteBuffer.wrap(ad1Bytes).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
				double ad1Voltage = ad1Value / 100.0;

				//	logger.info("\u001B[34mDevice {} - Extracted AD1 Hex: {}\u001B[0m", deviceID, ad1Hex);
				//		logger.info("\u001B[34mDevice {} - Extracted AD1 Voltage: {:.2f} V\u001B[0m", deviceID, ad1Voltage);

				extractedParams.put("AD1", String.format("%.2f V", ad1Voltage));
				loggedParams.add("AD1"); // Mark as logged
			}
		}

		// Extract AD4 (Parameter ID: 0x19)
		for (int i = 0; i < buffer.length - 3; i++) {
			if (buffer[i] == (byte) 0x19 && !loggedParams.contains("AD4")) {
				byte[] ad4Bytes = Arrays.copyOfRange(buffer, i + 1, i + 3);
				String ad4Hex = bytesToHex(ad4Bytes);
				int ad4Value = ByteBuffer.wrap(ad4Bytes).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
				double ad4Voltage = ad4Value / 100.0;
				double batteryPercentage = Math.max(0, Math.min((ad4Voltage - 3.4) / 0.8 * 100, 100));

				//		logger.info("\u001B[32mDevice {} - Extracted AD4 Hex: {}\u001B[0m", deviceID, ad4Hex);
				//		logger.info("\u001B[32mDevice {} - Extracted AD4 Voltage: {:.2f} V\u001B[0m", deviceID, ad4Voltage);
				//		logger.info("\u001B[32mDevice {} - Extracted Battery Percentage: {:.0f}%\u001B[0m", deviceID,
				//	batteryPercentage);

				extractedParams.put("AD4", String.format("%.2f V (%.0f%%)", ad4Voltage, batteryPercentage));
				loggedParams.add("AD4"); // Mark as logged
			}
		}

		// Extract AD5 (Parameter ID: 0x1A)
		for (int i = 0; i < buffer.length - 3; i++) {
			if (buffer[i] == (byte) 0x1A && !loggedParams.contains("AD5")) {
				byte[] ad5Bytes = Arrays.copyOfRange(buffer, i + 1, i + 3);
				String ad5Hex = bytesToHex(ad5Bytes);
				int ad5Value = ByteBuffer.wrap(ad5Bytes).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
				double ad5Voltage = ad5Value / 100.0;

				//		logger.info("\u001B[36mDevice {} - Extracted AD5 Hex: {}\u001B[0m", deviceID, ad5Hex);
				//	logger.info("\u001B[36mDevice {} - Extracted AD5 Voltage: {:.2f} V\u001B[0m", deviceID, ad5Voltage);

				extractedParams.put("AD5", String.format("%.2f V", ad5Voltage));
				loggedParams.add("AD5"); // Mark as logged
			}
		}

		return extractedParams;
	}

	public Map<String, Object> extractDrivingDirectionAndHDOP(byte[] buffer, String deviceID) {
		Map<String, Object> extractedParams = new HashMap<>();
		Set<String> loggedParams = new HashSet<>(); // To prevent duplicate logs

		// Extract Driving Direction (Parameter ID: 0x09, must end with 0x0A)
		for (int i = 0; i < buffer.length - 3; i++) {
			if (buffer[i] == (byte) 0x09 && !loggedParams.contains("DrivingDirection")) {
				if ((i + 3) < buffer.length && buffer[i + 3] == (byte) 0x0A) {
					byte[] directionBytes = Arrays.copyOfRange(buffer, i + 1, i + 3);
					String directionHex = bytesToHex(directionBytes);
					int directionValue = ByteBuffer.wrap(directionBytes).order(ByteOrder.LITTLE_ENDIAN).getShort()
							& 0xFFFF;

					//	logger.info("\u001B[35mDevice {} - Extracted Driving Direction Hex: {}\u001B[0m", deviceID,
					//	directionHex);
							//	logger.info("\u001B[35mDevice {} - Calculated Driving Direction: {} degrees\u001B[0m", deviceID,
					//	directionValue);

					extractedParams.put("DrivingDirection", directionValue);
					loggedParams.add("DrivingDirection"); // Mark as logged
				}
			}
		}

		// Extract HDOP (Parameter ID: 0x0A, must end with 0x0B)
		for (int i = 0; i < buffer.length - 3; i++) {
			if (buffer[i] == (byte) 0x0A && !loggedParams.contains("HDOP")) {
				if ((i + 3) < buffer.length && buffer[i + 3] == (byte) 0x0B) {
					byte[] hdopBytes = Arrays.copyOfRange(buffer, i + 1, i + 3);
					String hdopHex = bytesToHex(hdopBytes);
					int hdopValue = ByteBuffer.wrap(hdopBytes).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
					double hdopPrecision = hdopValue / 10.0; // Convert to correct precision

					//	logger.info("\u001B[33mDevice {} - Extracted HDOP Hex: {}\u001B[0m", deviceID, hdopHex);
					//	logger.info("\u001B[33mDevice {} - Calculated HDOP: {:.1f}\u001B[0m", deviceID, hdopPrecision);

					extractedParams.put("HDOP", hdopPrecision);
					loggedParams.add("HDOP"); // Mark as logged
				}
			}
		}

		return extractedParams;
	}

	public Map<String, Object> extractAdditionalParameters(byte[] buffer, String deviceID) {
		Map<String, Object> extractedParams = new HashMap<>();
		Set<Byte> processedParameters = new HashSet<>(); // To track processed parameter IDs

		for (int i = 0; i < buffer.length - 1; i++) {
			byte paramID = buffer[i];
			if (processedParameters.contains(paramID)) {
				continue; // Skip already processed parameters
			}

			switch (paramID) {
			case 0x05: // GPS Positioning Status
				byte gpsStatus = buffer[i + 1];
				String gpsStatusHex = String.format("0x%02X", gpsStatus);
				//	logger.info("\u001B[35mDevice {} - GPS Positioning Status Hex: {}\u001B[0m", deviceID, gpsStatusHex);

				String gpsPositioningStatus = gpsStatus == 0x01 ? "Valid" : "Invalid";
				//		logger.info("\u001B[33mDevice {} - GPS Positioning Status: {}\u001B[0m", deviceID,
				//			gpsPositioningStatus);

				extractedParams.put("GPSPositioningStatus", gpsPositioningStatus);
				processedParameters.add(paramID);
				break;

			case 0x06: // Number of Satellites
				byte satellites = buffer[i + 1];
				String satellitesHex = String.format("0x%02X", satellites);
				//	logger.info("\u001B[36mDevice {} - Number of Satellites Hex: {}\u001B[0m", deviceID, satellitesHex);

				int numberOfSatellites = satellites & 0xFF;
				//	logger.info("\u001B[34mDevice {} - Number of Satellites: {}\u001B[0m", deviceID, numberOfSatellites);

				extractedParams.put("NumberOfSatellites", numberOfSatellites);
				processedParameters.add(paramID);
				break;

			case 0x07: // GSM Signal Strength
				byte gsmSignal = buffer[i + 1];
				String gsmSignalHex = String.format("0x%02X", gsmSignal);
				//	logger.info("\u001B[32mDevice {} - GSM Signal Strength Hex: {}\u001B[0m", deviceID, gsmSignalHex);

				int signalStrength = gsmSignal & 0xFF;
				//	logger.info("\u001B[31mDevice {} - GSM Signal Strength: {}\u001B[0m", deviceID, signalStrength);

				extractedParams.put("GSMSignalStrength", signalStrength);
				processedParameters.add(paramID);
				break;

			case 0x14: // Output Port Status
				byte outputStatus = buffer[i + 1];
				String outputHex = String.format("0x%02X", outputStatus);
				//	logger.info("\u001B[35mDevice {} - Output Port Status Hex: {}\u001B[0m", deviceID, outputHex);

				String outputBinary = String.format("%8s", Integer.toBinaryString(outputStatus & 0xFF)).replace(' ',
						'0');
				//	logger.info("\u001B[33mDevice {} - Output Port Status (Binary): {}\u001B[0m", deviceID, outputBinary);

				extractedParams.put("OutputPortStatus", outputBinary);
				processedParameters.add(paramID);
				break;

			case 0x15: // Input Port Status
				byte inputStatus = buffer[i + 1];
				String inputHex = String.format("0x%02X", inputStatus);
				//	logger.info("\u001B[36mDevice {} - Input Port Status Hex: {}\u001B[0m", deviceID, inputHex);

				String inputBinary = String.format("%8s", Integer.toBinaryString(inputStatus & 0xFF)).replace(' ', '0');
				//	logger.info("\u001B[34mDevice {} - Input Port Status (Binary): {}\u001B[0m", deviceID, inputBinary);

				extractedParams.put("InputPortStatus", inputBinary);
				processedParameters.add(paramID);
				break;

			case 0x1B: // Geo-fence Number
				byte geoFence = buffer[i + 1];
				String geoFenceHex = String.format("0x%02X", geoFence);
			//	logger.info("\u001B[32mDevice {} - Geo-fence Number Hex: {}\u001B[0m", deviceID, geoFenceHex);

				int geoFenceNumber = geoFence & 0xFF;
				//	logger.info("\u001B[31mDevice {} - Geo-fence Number: {}\u001B[0m", deviceID, geoFenceNumber);

				extractedParams.put("GeoFenceNumber", geoFenceNumber);
				processedParameters.add(paramID);
				break;

			default:
				break; // Ignore unknown parameters
			}
		}

		return extractedParams;
	}
	
	
	
	/**
	 * Processes the ignition state from the packet buffer and logs whether it is ON or OFF.
	 *
	 * @param buffer   The byte array containing the packet data.
	 * @param deviceID The unique identifier of the device.
	 * @return The state ("ON" or "OFF") if successfully processed, or null if not found.
	 */
	public String processStateChange(byte[] buffer, String deviceID) {
	    String state = null;

	    // Iterate through the buffer to locate the 0x15 marker
	    for (int i = 0; i < buffer.length - 1; i++) {
	        if (buffer[i] == (byte) 0x15) { // Check for the 0x15 marker
	            byte stateByte = buffer[i + 1]; // The byte immediately following 0x15

	            // Determine the state based on the value of the stateByte
	            if (stateByte == (byte) 0x01) {
	                state = "ON";
	            } else if (stateByte == (byte) 0x00) {
	                state = "OFF";
	            } else {
	                logger.warn("Device {} - Invalid state byte following 0x15 marker: 0x{}", deviceID, String.format("%02X", stateByte));
	                break; // Exit the loop for invalid state
	            }

	            // Log the state change with a clear message
	            logger.info(
	                "\u001B[1;36mDevice {}\u001B[0m - Ignition (ACC) State: \u001B[{}m{}\u001B[0m",
	                deviceID,
	                "ON".equals(state) ? "1;32" : "1;31", // Green for ON, Red for OFF
	                state
	            );

	            break; // Exit the loop after processing the first valid state change
	        }
	    }

	    // Log a warning if the 0x15 marker was not found in the buffer
	    if (state == null) {
	        logger.warn("Device {} - Ignition state marker (0x15) not found in the packet.", deviceID);
	    }

	    return state;
	}


	/**
	 * Converts a byte array to a hex string for debugging.
	 *
	 * @param bytes The byte array to convert.
	 * @return The hex string representation of the byte array.
	 */
	private String bytesToHex(byte[] bytes) {
		StringBuilder hexBuilder = new StringBuilder();
		for (byte b : bytes) {
			hexBuilder.append(String.format("0x%02X ", b));
		}
		return hexBuilder.toString().trim();
	}
	
	

}