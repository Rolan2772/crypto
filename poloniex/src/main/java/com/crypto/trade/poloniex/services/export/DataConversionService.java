package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.storage.model.TimeFrameStorage;

public interface DataConversionService {

    StringBuilder convert(TimeFrameStorage timeFrameStorage);
}
