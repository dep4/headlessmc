package me.earth.headlessmc.logging.handlers;

import me.earth.headlessmc.logging.LoggingService;

import java.util.logging.Handler;

/**
 * Interface that can be implemented by a {@link Handler}
 * to show that it should not be removed by a {@link LoggingService}.
 *
 * @see HmcFileHandler
 * @see HmcStreamHandler
 */
public interface HmcHandler {

}