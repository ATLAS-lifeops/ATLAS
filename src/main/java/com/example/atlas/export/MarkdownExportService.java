package com.example.atlas.export;

import com.example.atlas.privacy.PrivacyExport;
import com.example.atlas.privacy.PrivacyService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MarkdownExportService {

    private final ObjectProvider<PrivacyService> privacyService;

    public MarkdownExportService(ObjectProvider<PrivacyService> privacyService) {
        this.privacyService = privacyService;
    }

    public String exportUserMarkdown(UUID userId) {
        PrivacyService service = privacyService.getIfAvailable();
        if (service == null) {
            throw new IllegalStateException("Privacy export is not available without persistence.");
        }
        PrivacyExport export = service.export(userId);
        return export.markdown();
    }
}
