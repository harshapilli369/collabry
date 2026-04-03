package com.group4.backend.service.campaign;

import com.group4.backend.model.Campaign;
import com.group4.backend.model.CollaborationInvitation;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.model.Payment;
import com.group4.backend.model.User;
import com.group4.backend.repository.campaign.CampaignRepository;
import com.group4.backend.repository.profile.InfluencerProfileRepository;
import com.group4.backend.repository.campaign.InvitationRepository;
import com.group4.backend.repository.payment.PaymentRepository;
import com.group4.backend.repository.user.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class CampaignReportService {

    private final CampaignRepository campaignRepository;
    private final InvitationRepository invitationRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final InfluencerProfileRepository influencerProfileRepository;

    public CampaignReportService(CampaignRepository campaignRepository,
                                 InvitationRepository invitationRepository,
                                 PaymentRepository paymentRepository,
                                 UserRepository userRepository,
                                 InfluencerProfileRepository influencerProfileRepository) {
        this.campaignRepository = campaignRepository;
        this.invitationRepository = invitationRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.influencerProfileRepository = influencerProfileRepository;
    }

    public byte[] generateCampaignReportPdf(Long brandUserId, Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .filter(c -> brandUserId.equals(c.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found for this brand"));

        List<CollaborationInvitation> invitations = invitationRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId);
        List<Payment> payments = paymentRepository.findByCampaignIdOrderByDueDateAsc(campaignId);

        List<String> lines = new ArrayList<>();
        lines.add("Campaign Report");
        lines.add("Generated: " + Instant.now());
        lines.add("");
        lines.add("Campaign details");
        lines.add("Name: " + valueOrDash(campaign.getName()));
        lines.add("Status: " + valueOrDash(campaign.getStatus()));
        lines.add("Goal: " + valueOrDash(campaign.getCampaignGoal()));
        lines.add("Budget range: " + valueOrDash(campaign.getBudgetRange()));
        lines.add("Preferred content: " + valueOrDash(campaign.getPreferredContentTypes()));
        lines.add("Start date: " + valueOrDash(campaign.getStartDate()));
        lines.add("End date: " + valueOrDash(campaign.getEndDate()));
        lines.add("Requested influencers: " + valueOrDash(campaign.getNumberOfInfluencers()));
        lines.add("");

        lines.add("Invited influencers");
        if (invitations.isEmpty()) {
            lines.add("- None");
        } else {
            for (CollaborationInvitation inv : invitations) {
                String influencerLabel = influencerLabel(inv.getInfluencerId());
                lines.add("- " + influencerLabel + " | status: " + inv.getStatus());
            }
        }
        lines.add("");

        lines.add("Payment summary");
        if (payments.isEmpty()) {
            lines.add("No payments found for this campaign.");
        } else {
            BigDecimal total = BigDecimal.ZERO;
            BigDecimal paid = BigDecimal.ZERO;
            for (Payment payment : payments) {
                total = total.add(orZero(payment.getAmount()));
                if ("PAID".equals(String.valueOf(payment.getStatus()))) {
                    paid = paid.add(orZero(payment.getAmount()));
                }
                lines.add("- " + valueOrDash(payment.getMilestoneName())
                        + " | " + payment.getStatus()
                        + " | $" + orZero(payment.getAmount()));
            }
            lines.add("Total scheduled: $" + total);
            lines.add("Total paid: $" + paid);
            lines.add("Outstanding: $" + total.subtract(paid));
        }

        return buildCampaignPdf(lines);
    }

    private String influencerLabel(Long userId) {
        InfluencerProfile profile = influencerProfileRepository.findByUserId(userId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        String name = profile != null ? profile.getName() : null;
        String email = user != null ? user.getEmail() : null;
        if (name != null && email != null) return name + " (" + email + ")";
        if (name != null) return name;
        if (email != null) return email;
        return "User #" + userId;
    }

    private static BigDecimal orZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private static String valueOrDash(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    /** Minimal PDF from plain text lines; no extra PDF library dependency. */
    private static byte[] buildCampaignPdf(List<String> lines) {
        StringBuilder textOps = new StringBuilder();
        textOps.append("BT\n/F1 11 Tf\n50 780 Td\n");
        boolean first = true;
        for (String line : lines) {
            if (!first) {
                textOps.append("0 -14 Td\n");
            }
            first = false;
            textOps.append("(").append(escapePdfText(line)).append(") Tj\n");
        }
        textOps.append("ET");

        byte[] streamBytes = textOps.toString().getBytes(StandardCharsets.ISO_8859_1);
        List<Integer> offsets = new ArrayList<>();
        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");

        offsets.add(pdf.length());
        pdf.append("1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n");
        offsets.add(pdf.length());
        pdf.append("2 0 obj << /Type /Pages /Count 1 /Kids [3 0 R] >> endobj\n");
        offsets.add(pdf.length());
        pdf.append("3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] ");
        pdf.append("/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n");
        offsets.add(pdf.length());
        pdf.append("4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n");
        offsets.add(pdf.length());
        pdf.append("5 0 obj << /Length ").append(streamBytes.length).append(" >> stream\n");
        pdf.append(new String(streamBytes, StandardCharsets.ISO_8859_1));
        pdf.append("\nendstream endobj\n");

        int xrefStart = pdf.length();
        pdf.append("xref\n0 6\n");
        pdf.append("0000000000 65535 f \n");
        for (Integer offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }
        pdf.append("trailer << /Size 6 /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefStart).append("\n%%EOF");

        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String escapePdfText(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }
}
