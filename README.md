Using libotp
============

libotp is a simple java One-time password generator library, which can easily be used to facilitate two-factor authentication in Java application. 
The library current supports the standard RFC counter based (hotp) and time based (totp) algorithms. Custom OTP providers can also be written by implementing the 
__OTPProvider__ interface.

Example
-------

Below is an example of generating time-based password

> OTP.generate("" + key, "" + System.currentTimeMillis(), 6, "totp")

Following is an example of counter-based password

> OTP.generate(secret, "" + 2, 6, "hotp")