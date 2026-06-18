export function cloneDeep(value) {
  if (typeof structuredClone === "function") {
    return structuredClone(value);
  }
  return value == null ? value : JSON.parse(JSON.stringify(value));
}

export function downLoadXls(data, fileName) {
  const blob = data instanceof Blob ? data : new Blob([data]);
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = fileName || "download.xls";
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}

export function getRandom(length = 16) {
  const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  let result = "";
  for (let i = 0; i < length; i += 1) {
    result += chars[Math.floor(Math.random() * chars.length)];
  }
  return result;
}

export function sortStr(...args) {
  return args.map(item => String(item)).sort().join("");
}

export function sha1(input) {
  const message = unescape(encodeURIComponent(String(input)));
  const words = [];
  for (let i = 0; i < message.length; i += 1) {
    words[i >> 2] |= message.charCodeAt(i) << (24 - (i % 4) * 8);
  }

  const bitLength = message.length * 8;
  words[bitLength >> 5] |= 0x80 << (24 - (bitLength % 32));
  words[((bitLength + 64 >> 9) << 4) + 15] = bitLength;

  let h0 = 0x67452301;
  let h1 = 0xefcdab89;
  let h2 = 0x98badcfe;
  let h3 = 0x10325476;
  let h4 = 0xc3d2e1f0;

  for (let i = 0; i < words.length; i += 16) {
    const w = new Array(80);
    for (let j = 0; j < 16; j += 1) {
      w[j] = words[i + j] || 0;
    }
    for (let j = 16; j < 80; j += 1) {
      w[j] = rotateLeft(w[j - 3] ^ w[j - 8] ^ w[j - 14] ^ w[j - 16], 1);
    }

    let a = h0;
    let b = h1;
    let c = h2;
    let d = h3;
    let e = h4;

    for (let j = 0; j < 80; j += 1) {
      const temp = add32(
        add32(rotateLeft(a, 5), sha1Function(j, b, c, d)),
        add32(add32(e, w[j]), sha1Constant(j))
      );
      e = d;
      d = c;
      c = rotateLeft(b, 30);
      b = a;
      a = temp;
    }

    h0 = add32(h0, a);
    h1 = add32(h1, b);
    h2 = add32(h2, c);
    h3 = add32(h3, d);
    h4 = add32(h4, e);
  }

  return [h0, h1, h2, h3, h4].map(toHex).join("");
}

function rotateLeft(value, bits) {
  return (value << bits) | (value >>> (32 - bits));
}

function add32(a, b) {
  return (a + b) & 0xffffffff;
}

function sha1Function(index, b, c, d) {
  if (index < 20) {
    return (b & c) | (~b & d);
  }
  if (index < 40) {
    return b ^ c ^ d;
  }
  if (index < 60) {
    return (b & c) | (b & d) | (c & d);
  }
  return b ^ c ^ d;
}

function sha1Constant(index) {
  if (index < 20) {
    return 0x5a827999;
  }
  if (index < 40) {
    return 0x6ed9eba1;
  }
  if (index < 60) {
    return 0x8f1bbcdc;
  }
  return 0xca62c1d6;
}

function toHex(value) {
  return (value >>> 0).toString(16).padStart(8, "0");
}
