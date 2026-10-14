# Charlotte email address — SAFE design checkpoint

Under Settings > Email > Charlotte Email Address, each authorised user may enter, edit, verify, or disconnect a dedicated email address for Charlotte. The address is a user-configured value, not a mailbox automatically created by the application.

- Validate email address format, but never treat format validation as ownership verification.
- Require provider-supported OAuth or an approved secure authentication method before accessing a mailbox; do not request or store mailbox passwords in plain text.
- Show distinct states: not configured, pending verification, connected, expired, and disconnected.
- Isolate accounts and permissions by authorised user. The Creator controls any shared Charlotte identity mailbox.
- Sending is disabled until the account is connected and permissions are granted. Sensitive, external or consequential messages require explicit approval; routine automatic notifications require advance opt-in.
- Allow revocation, disconnect, and deletion of locally stored connection data. Protect tokens in platform-secure storage and keep an auditable history of outbound actions.
- Never claim an address is available, created, verified, or able to send mail without provider confirmation.

Status: SAFE conceptual specification only. Settings UI, account integration, delivery, and tests remain unimplemented until independently verified.
