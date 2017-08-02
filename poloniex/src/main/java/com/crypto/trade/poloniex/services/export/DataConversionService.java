package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.storage.TimeFrameStorage;

public interface DataConversionService {

    StringBuilder convert(TimeFrameStorage timeFrameStorage);
}
