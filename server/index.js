const express = require("express");
const http = require("http");
const { Server } = require("socket.io");
const crypto = require("crypto");

const app = express();
app.get("/", (_req,res)=>res.json({name:"Mafia Shab Akhar",status:"online",mode:"public-matchmaking",playersPerMatch:10}));
app.get("/health", (_req,res)=>res.json({ok:true,waiting:queue.length,matches:matches.size}));
const server = http.createServer(app);
const io = new Server(server,{cors:{origin:"*",methods:["GET","POST"]},pingInterval:25000,pingTimeout:20000});

const MAX=10, queue=[], matches=new Map(), playerMatch=new Map(), profiles=new Map();
const roles=["مافیا","مافیا","مافیا","دکتر","کارآگاه","شهروند","شهروند","شهروند","شهروند","شهروند"];
const cleanName=v=>String(v||"بازیکن").trim().replace(/[<>]/g,"").slice(0,18)||"بازیکن";
function publicQueue(){io.emit("queue_update",{count:queue.length,capacity:MAX,players:queue.map(id=>({name:profiles.get(id)?.name||"بازیکن"}))});}
function removeFromQueue(id){const i=queue.indexOf(id);if(i>=0)queue.splice(i,1);publicQueue();}
function shuffle(a){for(let i=a.length-1;i>0;i--){const j=crypto.randomInt(i+1);[a[i],a[j]]=[a[j],a[i]];}return a;}
function makeMatch(){
 if(queue.length<MAX)return;
 const ids=queue.splice(0,MAX), matchId=crypto.randomUUID(), assigned=shuffle([...roles]);
 const players=ids.map((id,i)=>({id,name:profiles.get(id)?.name||"بازیکن",role:assigned[i],alive:true,connected:true}));
 const match={id:matchId,players,phase:"شب معارفه",round:0,createdAt:Date.now(),nightActions:{},votes:{},messages:[]};
 matches.set(matchId,match);ids.forEach(id=>playerMatch.set(id,matchId));
 players.forEach(p=>io.to(p.id).emit("match_started",{matchId,players:players.map(x=>({name:x.name,alive:x.alive})),myRole:p.role,phase:match.phase,round:0}));
 io.to(ids).emit("game_event",{type:"notice",text:"ظرفیت اتاق تکمیل شد؛ مسابقه آغاز شد."});
 setTimeout(()=>beginDay(matchId),12000);
 publicQueue();
}
function beginDay(id){const m=matches.get(id);if(!m)return;m.phase=m.round===0?"روز معارفه":"روز";io.to(m.players.map(p=>p.id)).emit("phase_changed",{phase:m.phase,round:m.round+1,players:m.players.map(p=>({name:p.name,alive:p.alive}))});}
io.on("connection",socket=>{
 socket.emit("queue_update",{count:queue.length,capacity:MAX,players:queue.map(id=>({name:profiles.get(id)?.name||"بازیکن"}))});
 socket.on("join_queue",(data={})=>{
  if(playerMatch.has(socket.id)||queue.includes(socket.id))return;
  profiles.set(socket.id,{name:cleanName(data.name)});queue.push(socket.id);socket.emit("queue_joined",{position:queue.length,capacity:MAX});publicQueue();makeMatch();
 });
 socket.on("leave_queue",()=>removeFromQueue(socket.id));
 socket.on("chat_message",data=>{
  const mid=playerMatch.get(socket.id),m=matches.get(mid);if(!m||!["روز","روز معارفه","بحث"].includes(m.phase))return;
  const p=m.players.find(x=>x.id===socket.id);if(!p?.alive)return;
  const msg=String(data?.text||"").trim().slice(0,350);if(!msg)return;
  const item={name:p.name,text:msg,at:Date.now()};m.messages.push(item);
  io.to(m.players.map(x=>x.id)).emit("game_event",{type:"chat",message:item});
 });
 socket.on("disconnect",()=>{
  removeFromQueue(socket.id);
  const mid=playerMatch.get(socket.id),m=matches.get(mid);
  if(m){const p=m.players.find(x=>x.id===socket.id);if(p)p.connected=false;io.to(m.players.map(x=>x.id)).emit("game_event",{type:"disconnect",name:p?.name||"بازیکن"});}
  playerMatch.delete(socket.id);profiles.delete(socket.id);
 });
});
const PORT=process.env.PORT||3000;
server.listen(PORT,"0.0.0.0",()=>console.log("Mafia Shab Akhar server listening on "+PORT));
