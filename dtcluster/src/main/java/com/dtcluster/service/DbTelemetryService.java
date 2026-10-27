package com.dtcluster.service;

import com.dtcluster.dao.TelemetryDao;
import com.dtcluster.model.TelemetryReading;

public class DbTelemetryService implements TelemetryService {
    @Override
    public boolean ingest(TelemetryReading r) {
        return TelemetryDao.record(r);
    }
}
