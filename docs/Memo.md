# Dev Memo
#### prerequisite:

1. JDK(JRE) 1.8.0, install
2. Tomcat 8, download as open folder
3. MySQL Community Server 5.7.11, install

#### Password strategy

1. Encrypt pw with CSPRNG salt, store mixture pw and salt
2. When verify user pw, encrypt pw with salt, then compare