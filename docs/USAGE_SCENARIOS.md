# Usage Scenarios

This document walks through every feature in Collabry. It's referenced from the main README.

---

## Table of Contents

1. [Onboarding — common flow](#1-onboarding--common-flow)
2. [Brand flows](#2-brand-flows)
3. [Influencer flows](#3-influencer-flows)
4. [Admin flows](#4-admin-flows)

---

## 1. Onboarding — common flow

### 1.1 New user registration and profile setup

The landing page has a hero section with a "Sign Up" button in the nav bar. Any new visitor can start registration from here.

1. Click **Sign Up** on the landing page.
2. Fill in your name, email, and password, then pick your role — **Brand** or **Influencer**.
3. Hit **Register**. A confirmation email goes out (or the link prints to the console in local dev without SMTP).
4. Click the confirmation link to activate your account.
5. On first login, a role-selection toggle redirects you to the right profile setup:
   - **Brand** → fills in Company Name, Industry, Website, Email
   - **Influencer** → fills in Name, Niche, Bio, Location, Social Media handles, Follower Count, Rate

### 1.2 Account recovery / password reset

1. Click **Forgot Password** on the login page.
2. Enter your email and click **Send Reset Link**.
3. A secure token-based reset link is emailed to you (or printed to console in dev mode).
4. Click the link, enter your new password, and submit.
5. The backend validates the token, hashes the new password, and redirects you to login. Tokens expire after 24 hours and can only be used once.

### 1.3 Requesting a verified badge

Influencers can request a "Blue Checkmark" verified status from their profile page.

1. From the influencer profile, click **Request Verification**.
2. The request goes to the admin queue with status **PENDING**.
3. Once the admin approves it, a blue checkmark appears on the public profile, giving brands more confidence when browsing.
4. If rejected, the admin supplies a reason (e.g. "Link to social media is broken") and the influencer can re-apply after fixing the issue.

---

## 2. Brand flows

### 2.1 Creating a campaign

From the brand dashboard, click **Create Campaign**. The form takes:

- Campaign title (e.g. "Summer Tech Launch")
- Description and brief
- Budget range (e.g. $1,000–$5,000)
- Target niche / audience
- Start and end dates

On submission the campaign is saved as **DRAFT** and you're redirected to My Campaigns where it shows up immediately.

### 2.2 Finding influencers (manual search and AI)

**Manual search:**

1. Go to **Find Influencers**.
2. Use the filter bar — Niche, Location, Min Followers, Max Followers, Engagement rate.
3. Hit **Search**. Results come back as a responsive grid of influencer cards, each showing name, niche, follower count, and star rating.
4. Click **View Profile** on any card to see their full portfolio page.

**AI Matchmaker:**

1. On the Find Influencers page, click **AI Match**.
2. Select one of your DRAFT campaigns. The system reads the campaign's niche, budget, and goals.
3. The AI Matchmaker slides open and shows the top-matched influencers with a numerical **Match Score** (e.g. 95%, 82%) and a plain-English reason for each match (e.g. "Robin's niche directly matches the campaign topic of Winter Fashion collection, and her rate is within the budget range").
4. Click **Invite** directly from the match results to send an invitation.

### 2.3 Sending and withdrawing invitations

Once you've found an influencer you want to work with:

1. Click **Invite** on their profile or from the AI match results.
2. A modal opens — select the campaign, write a personalised message, set the proposed budget, timeline, and any required deliverables (e.g. "1 Instagram Reel, 2 Stories").
3. Click **Send Invitation**. The invite lands in the brand's **Sent Invitations** panel with status **PENDING**.
4. To cancel before the influencer responds, hit the red **Withdraw** button on the invitation card — this permanently closes the invite.

### 2.4 Campaign lifecycle management

The **My Campaigns** dashboard lists all campaigns with colour-coded status tags:

| Action | When available | Result |
|--------|---------------|--------|
| **Publish** | Campaign is DRAFT | Status → ACTIVE, influencers can now be invited |
| **Complete** | Campaign is ACTIVE | Status → COMPLETED, no more changes |
| **Cancel** | DRAFT or ACTIVE | Status → CANCELLED, locks all activity |

Once COMPLETED or CANCELLED the campaign is frozen. The dashboard also shows active invitations and collaboration counts per campaign.

### 2.5 Processing payments

The brand's **Payments** portal tracks all milestone payments to influencers.

1. Go to **Payments** → click **Create Payment**.
2. Enter the campaign, influencer, milestone name (e.g. "Content Delivery", "Final Review"), amount, and due date.
3. The payment appears in the table with status **PENDING** alongside total disbursed and delayed payment summaries.
4. Once the influencer delivers, mark it as **PAID**. An invoice is generated and can be downloaded from the Actions column.

---

## 3. Influencer flows

### 3.1 Influencer dashboard overview

Right after login, the influencer lands on their dashboard which surfaces:

- Count of **Pending Invitations** needing a response
- Count of **Active Collaborations** in progress
- Charts showing monthly collaboration activity

This lets the influencer immediately see what needs attention without digging through menus.

### 3.2 Handling invitations (accept / reject / negotiate)

1. Go to **My Invitations**. Invitations are sorted by status — Pending, Accepted, Rejected, Negotiating.
2. Click on any invitation to open the detail view showing campaign info, brand details, proposed budget, timeline, and required deliverables.
3. Three options:
   - **Accept** → status flips to CONFIRMED, a collaboration record is created
   - **Reject** → closes the invitation
   - **Negotiate** → opens a counter-offer form

**Counter-offer flow:**

On the negotiation screen, the influencer proposes a revised amount (e.g. $550 instead of $500) and an adjusted timeline. On submit, the invitation moves to **NEGOTIATING**. The brand is notified and can either accept the new terms or counter again. Once the brand clicks **Accept**, the status becomes **CONFIRMED** and the collaboration begins.

### 3.3 Fulfilling collaborations and receiving payouts

Once an invitation is CONFIRMED, a collaboration record appears in **My Collaborations** with status **ACTIVE**.

1. Create the content on the required platform (Instagram, YouTube, etc.).
2. In **My Collaborations**, find the active collaboration and click **Submit Deliverable**.
3. Paste the content URL and add any notes for the brand.
4. Status changes to **SUBMITTED** — the brand reviews it.
5. When the brand approves, status becomes **APPROVED** and escrow funds are released.
6. Go to **My Payments** to see the full ledger of earned funds and any amounts still pending release.

---

## 4. Admin flows

### 4.1 Platform monitoring

Log in as admin (admin@collabry.com / password123). The admin dashboard shows:

- Total registered users (brands + influencers)
- Total active campaigns
- Pending verification requests
- Recent signups table with email, role, join date, and active flag

### 4.2 Verification review

1. Navigate to the **Verification Requests** tab.
2. Each pending request shows the influencer's full profile details — website link, social media handles, bio — for manual review.
3. Click the green checkmark to **Approve** → the influencer immediately gets a Blue Checkmark on their public profile.
4. Click the red cross to **Reject** → a reason must be supplied (e.g. "Link to social media is broken"). The influencer is notified and can re-apply after fixing the issue.

### 4.3 User management and platform safety

1. Go to **User Management** in the admin panel.
2. The table lists all users with email, role, join date, active status, and a flagged indicator.
3. If a user is reported or behaves suspiciously, the admin can click **Flag** to mark them for investigation while keeping them active.
4. For confirmed violations, click **Deactivate** — this immediately sets the user's ACTIVE flag to false in the database and invalidates their security token, booting them from the platform instantly.
5. This two-tier system (flag first, deactivate if serious) ensures proportional responses to misconduct.
