package com.crypto.trade.poloniex.services.export;

import java.util.Collection;

public interface ExcessDataExportService<T> {

    void exportExcessData(String name, Collection<T> data);

}
