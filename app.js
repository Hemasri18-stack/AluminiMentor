const state={students:[],mentors:[],sessions:[],tags:[],mentorships:[]};
const apis={students:studentAPI,mentors:alumniAPI,sessions:sessionAPI,tags:tagAPI,mentorships:mentorshipAPI};
const names={students:"Student",mentors:"Mentor",sessions:"Session",tags:"Interest Tag"};
const schemas={
 students:[["name","Name","text"],["email","Email","email"],["department","Department","text"]],
 mentors:[["name","Name","text"],["email","Email","email"],["maxMentees","Maximum mentees","number"]],
 sessions:[["mentorshipPairId","Mentorship ID","number"],["topic","Topic","text"],["sessionDate","Session date and time","datetime-local"],["durationMinutes","Duration (minutes)","number"]],
 tags:[["name","Tag Name","text"]]
};
const $=s=>document.querySelector(s), $$=s=>document.querySelectorAll(s);
function esc(x){return String(x??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"}[c]))}
function idOf(x){return x?.id??x?.studentId??x?.alumniId??x?.tagId??x?.sessionId}
function titleOf(x,f="Untitled"){return x?.name||x?.title||x?.fullName||x?.username||f}
function toast(m,err=false){let e=$("#toast");e.textContent=m;e.className="toast show "+(err?"err":"");setTimeout(()=>e.className="toast",2800)}
function page(p){$$(".page").forEach(x=>x.classList.toggle("active",x.dataset.view===p));$$(".nav").forEach(x=>x.classList.toggle("active",x.dataset.page===p));location.hash=p;$(".stage").classList.remove("turn");void $(".stage").offsetWidth;$(".stage").classList.add("turn");$("#sidebar").classList.remove("open")}
function card(type,x){
 let id=idOf(x), icon=type==="students"?"🎓":type==="mentors"?"💻":type==="sessions"?"📅":"🏷️";
 let rows=Object.entries(x||{}).filter(([k])=>!["id","createdAt","updatedAt"].includes(k)).slice(0,4);
 return `<article class="record"><div class="recordtop"><span class="recicon">${icon}</span><small>#${esc(id??"—")}</small></div><h3>${esc(titleOf(x,names[type]))}</h3><div class="fields">${rows.map(([k,v])=>`<div><small>${esc(k)}</small><span>${esc(typeof v==="object"?JSON.stringify(v):v)}</span></div>`).join("")}</div><div class="actions"><button onclick='editRec(${JSON.stringify(type)},${JSON.stringify(x)})'>Edit</button><button class="danger" onclick='deleteRec(${JSON.stringify(type)},${JSON.stringify(id)})'>Delete</button></div></article>`
}
function render(type){
 let e=$("#"+type+"List"); if(!e)return;
 if(!state[type].length){e.innerHTML=`<div class="empty">📖<h3>No ${names[type]} records yet</h3><p>Add your first record to begin this chapter.</p></div>`;return}
 e.innerHTML=state[type].map(x=>card(type,x)).join("")
}
function renderTags(){
 let e=$("#tagsList"); if(!state.tags.length){e.innerHTML=`<div class="empty">🏷️<h3>No tags yet</h3></div>`;return}
 e.innerHTML=state.tags.map(x=>`<article class="tagcard">🏷️<b>${esc(titleOf(x,"Interest"))}</b><small>${esc(x.description||"Learning interest")}</small><button onclick='editRec("tags",${JSON.stringify(x)})'>Edit</button><button onclick='deleteRec("tags",${JSON.stringify(idOf(x))})'>×</button></article>`).join("")
}
async function load(type){
 try{let d=await apis[type].getAll();state[type]=Array.isArray(d)?d:(d?.content||[]);type==="tags"?renderTags():render(type);stats()}
 catch(e){type==="tags"?renderTags():render(type);return false}
 return true
}
function stats(){$("#studentCount").textContent=state.students.length;$("#mentorCount").textContent=state.mentors.length;$("#mentorshipCount").textContent=state.mentorships.length;$("#sessionCount").textContent=state.sessions.length}
async function loadAll(){
 $("#apiStatus").textContent="checking";
 const results=await Promise.all(Object.keys(state).map(load));
 const connected=results.every(Boolean);
 $("#apiStatus").textContent=connected?"connected":"unavailable";
 $("#dot").classList.toggle("online",connected);
}
function openForm(type,item=null){
 $("#modal").classList.remove("hidden");$("#modalEyebrow").textContent=item?"EDIT RECORD":"NEW RECORD";$("#modalTitle").textContent=(item?"Edit ":"Add ")+names[type];
 let f=$("#form");f.dataset.type=type;f.dataset.id=item?idOf(item):"";
 f.innerHTML=(schemas[type]||[]).map(([k,l,t])=>`<label>${l}<input name="${k}" type="${t}" value="${esc(item?.[k]??(k==="mentorshipPairId"?item?.mentorshipPair?.id:""))}" ${k==="mentorshipPairId"?'min="1"':''} required></label>`).join("")+ 
 `<div class="modalactions"><button type="button" class="light" data-close>Cancel</button><button class="primary">Save Changes →</button></div>`;
}
window.editRec=openForm;
window.deleteRec=async(type,id)=>{
 if(!id&&!confirm("Delete this record?"))return;
 if(!confirm(`Delete this ${names[type].toLowerCase()}?`))return;
 try{await apis[type].delete(id);toast("Deleted successfully");load(type)}catch(e){toast(e.message,true)}
}
$("#form").addEventListener("submit",async e=>{
 e.preventDefault();let f=e.currentTarget,type=f.dataset.type,id=f.dataset.id,d={};
 new FormData(f).forEach((v,k)=>{if(v!=="")d[k]=/^-?\d+(\.\d+)?$/.test(v)?Number(v):v});
 try{id?await apis[type].update(id,d):await apis[type].create(d);$("#modal").classList.add("hidden");toast("Saved successfully");load(type)}
 catch(e){toast(e.message,true)}
});
$("#findBtn").onclick=async()=>{
 let id=$("#matchId").value;if(!id)return toast("Enter a student ID",true);$("#matches").innerHTML='<div class="empty">✨ Finding the right chapter...</div>';
 try{let d=await mentorshipAPI.suggestions(id),a=Array.isArray(d)?d:(d?.suggestions||d?.content||[d]);$("#matches").innerHTML=a.filter(Boolean).map(x=>`<article class="matchcard">✨<div><small>MATCH</small><h3>${esc(titleOf(x,"Recommended Mentor"))}</h3><p>${esc(typeof x==="object"?JSON.stringify(x):x)}</p></div></article>`).join("")||'<div class="empty">🔎 No suggestions found</div>'}catch(e){$("#matches").innerHTML=`<div class="empty">📚<h3>Could not get suggestions</h3><p>${esc(e.message)}</p></div>`}
};
$("#reportBtn").onclick=async()=>{let r=$("#report");r.innerHTML="📊 Building report...";try{r.innerHTML="<pre>"+esc(JSON.stringify(await mentorshipAPI.report(),null,2))+"</pre>"}catch(e){r.innerHTML="<h3>Report unavailable</h3><p>"+esc(e.message)+"</p>"}};
$$(".nav").forEach(b=>b.onclick=()=>page(b.dataset.page));
$$("[data-go]").forEach(b=>b.onclick=()=>page(b.dataset.go));
$$("[data-add]").forEach(b=>b.onclick=()=>openForm(b.dataset.add));
$$("[data-close]").forEach(b=>b.onclick=()=>$("#modal").classList.add("hidden"));
$$("[data-refresh]").forEach(b=>b.onclick=loadAll);
$$("[data-filter]").forEach(inp=>inp.oninput=()=>{let t=inp.dataset.filter,q=inp.value.toLowerCase(),e=$("#"+t+"List");e.innerHTML=state[t].filter(x=>JSON.stringify(x).toLowerCase().includes(q)).map(x=>card(t,x)).join("")||'<div class="empty">🔎 No results</div>'});
$("#menu").onclick=()=>$("#sidebar").classList.toggle("open");
let h=location.hash.slice(1);page(["dashboard","students","mentors","find","sessions","tags","reports"].includes(h)?h:"dashboard");loadAll();
