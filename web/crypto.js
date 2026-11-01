/**
 * BrutalTrack E2EE Crypto Engine
 * Uses standard Web Crypto API (SubtleCrypto)
 * Compatible with Android javax.crypto (AES-256-GCM + PBKDF2WithHmacSHA256)
 */
class BrutalCrypto {
  static async deriveKey(passphrase, saltBytes) {
    const enc = new TextEncoder();
    const passKey = await window.crypto.subtle.importKey(
      "raw",
      enc.encode(passphrase),
      { name: "PBKDF2" },
      false,
      ["deriveKey"]
    );

    return await window.crypto.subtle.deriveKey(
      {
        name: "PBKDF2",
        salt: saltBytes,
        iterations: 65536,
        hash: "SHA-256"
      },
      passKey,
      { name: "AES-GCM", length: 256 },
      false,
      ["encrypt", "decrypt"]
    );
  }

  /**
   * Decrypts an incoming message object:
   * {
   *   s: "<base64 salt>",
   *   iv: "<base64 iv>",
   *   c: "<base64 ciphertext+tag>"
   * }
   */
  static async decryptPayload(envelope, passphrase) {
    if (!envelope || !envelope.s || !envelope.iv || !envelope.c) {
      throw new Error("Invalid payload format. Expected {s, iv, c}");
    }

    const salt = BrutalCrypto.base64ToUint8(envelope.s);
    const iv = BrutalCrypto.base64ToUint8(envelope.iv);
    const cipherBuffer = BrutalCrypto.base64ToUint8(envelope.c);

    const key = await BrutalCrypto.deriveKey(passphrase, salt);

    try {
      const decrypted = await window.crypto.subtle.decrypt(
        { name: "AES-GCM", iv: iv },
        key,
        cipherBuffer
      );
      const decText = new TextDecoder();
      return JSON.parse(decText.decode(decrypted));
    } catch (e) {
      throw new Error("DECRYPTION FAILED: Invalid passphrase or corrupted packet");
    }
  }

  /**
   * Encrypts a JavaScript object with a passphrase
   * Returns { s: base64, iv: base64, c: base64 }
   */
  static async encryptPayload(dataObj, passphrase) {
    const enc = new TextEncoder();
    const salt = window.crypto.getRandomValues(new Uint8Array(16));
    const iv = window.crypto.getRandomValues(new Uint8Array(12));

    const key = await BrutalCrypto.deriveKey(passphrase, salt);
    const plaintext = enc.encode(JSON.stringify(dataObj));

    const ciphertext = await window.crypto.subtle.encrypt(
      { name: "AES-GCM", iv: iv },
      key,
      plaintext
    );

    return {
      s: BrutalCrypto.uint8ToBase64(salt),
      iv: BrutalCrypto.uint8ToBase64(iv),
      c: BrutalCrypto.uint8ToBase64(new Uint8Array(ciphertext))
    };
  }

  static base64ToUint8(b64) {
    const binStr = atob(b64);
    const len = binStr.length;
    const bytes = new Uint8Array(len);
    for (let i = 0; i < len; i++) {
      bytes[i] = binStr.charCodeAt(i);
    }
    return bytes;
  }

  static uint8ToBase64(bytes) {
    let binary = "";
    const len = bytes.byteLength;
    for (let i = 0; i < len; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary);
  }
}

// Export for browser or node
if (typeof module !== "undefined" && module.exports) {
  module.exports = BrutalCrypto;
}
