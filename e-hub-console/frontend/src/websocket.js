import moment from "moment";

let Socket = "";
let globalCallback = null;
let globalWsUrl = null;
let setIntervalWesocketPush = null;

/** 建立连接 */
function createSocket(url) {
  if (!Socket) {
    Socket = new WebSocket(url);
    Socket.onopen = onopenWS;
    Socket.onmessage = onmessageWS;
    Socket.onerror = onerrorWS;
    Socket.onclose = oncloseWS;
  } else {
    console.log("websocket已连接");
  }
}

/** 打开WS之后发送心跳 */
function onopenWS() {
  console.log("发送心跳");
  sendPing(); // 发送心跳
}

/** 连接失败重连 */
function onerrorWS() {
  console.log("失败重连", moment().format("HH:mm:ss"));
  clearInterval(setIntervalWesocketPush);
  Socket.close();
  createSocket(globalWsUrl); // 重连
}

/** WS数据接收统一处理 */
function onmessageWS(e) {
  console.log(e, "接受消息");
  if (
    e.data !== "connection_success" &&
    e.data != "{}" &&
    e.data != "ping-success"
  ) {
    globalCallback(e.data);
  }
}

/** 发送数据 */
function sendWSPush(url, callback, eventTypeArr) {
  globalWsUrl = url;
  globalCallback = callback;
  const obj = {
    event: eventTypeArr,
  };
  createSocket(url);
  if (Socket !== null && Socket.readyState === 3) {
    Socket.close();
    createSocket(url); // 重连
  } else if (Socket.readyState === 1) {
    Socket.send(JSON.stringify(obj));
  } else if (Socket.readyState === 0) {
    setTimeout(() => {
      Socket.send(JSON.stringify(obj));
    }, 3000);
  }
}

/** 关闭WS */
function oncloseWS() {
  clearInterval(setIntervalWesocketPush);
  console.log("websocket已断开", moment().format("HH:mm:ss"));
  Socket.close();
  console.log(globalWsUrl, "globalWsUrl");
  createSocket(globalWsUrl); // 重连
}

/** 发送心跳 */
function sendPing() {
  Socket.send("ping");
  setIntervalWesocketPush = setInterval(() => {
    console.log("ping");
    Socket.send("ping");
  }, 21000);
}

export { sendWSPush };
