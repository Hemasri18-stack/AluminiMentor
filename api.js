const API_BASE_URL="http://localhost:8081/api";
async function apiRequest(endpoint,options={}){
 const r=await fetch(API_BASE_URL+endpoint,{headers:{"Content-Type":"application/json",...(options.headers||{})},...options});
 const t=await r.text(); let d=t; try{d=t?JSON.parse(t):null}catch{}
 if(!r.ok) {
  const validationErrors=d?.errors?Object.entries(d.errors).map(([field,message])=>`${field}: ${message}`).join("; "):null;
  throw new Error(typeof d==="string"?d:(d?.message||validationErrors||`HTTP ${r.status}`));
 }
 return d;
}
const studentAPI={getAll:()=>apiRequest("/students"),getById:id=>apiRequest(`/students/${id}`),create:d=>apiRequest("/students",{method:"POST",body:JSON.stringify(d)}),update:(id,d)=>apiRequest(`/students/${id}`,{method:"PUT",body:JSON.stringify(d)}),delete:id=>apiRequest(`/students/${id}`,{method:"DELETE"})};
const alumniAPI={getAll:()=>apiRequest("/alumni"),getById:id=>apiRequest(`/alumni/${id}`),create:d=>apiRequest("/alumni",{method:"POST",body:JSON.stringify(d)}),update:(id,d)=>apiRequest(`/alumni/${id}`,{method:"PUT",body:JSON.stringify(d)}),delete:id=>apiRequest(`/alumni/${id}`,{method:"DELETE"})};
const tagAPI={getAll:()=>apiRequest("/tags"),getById:id=>apiRequest(`/tags/${id}`),create:d=>apiRequest("/tags",{method:"POST",body:JSON.stringify(d)}),update:(id,d)=>apiRequest(`/tags/${id}`,{method:"PUT",body:JSON.stringify(d)}),delete:id=>apiRequest(`/tags/${id}`,{method:"DELETE"})};
const mentorshipAPI={getAll:()=>apiRequest("/mentorships"),getById:id=>apiRequest(`/mentorships/${id}`),create:({studentId,alumniId})=>apiRequest(`/mentorships?studentId=${encodeURIComponent(studentId)}&alumniId=${encodeURIComponent(alumniId)}`,{method:"POST"}),delete:id=>apiRequest(`/mentorships/${id}`,{method:"DELETE"}),suggestions:id=>apiRequest(`/mentorships/suggestions/student/${id}`),report:()=>apiRequest("/mentorships/report"),monthlyReport:()=>apiRequest("/mentorships/report/monthly")};
const sessionAPI={getAll:()=>apiRequest("/sessions"),getById:id=>apiRequest(`/sessions/${id}`),create:d=>{const {mentorshipPairId,...session}=d;return apiRequest(`/sessions?mentorshipPairId=${encodeURIComponent(mentorshipPairId)}`,{method:"POST",body:JSON.stringify(session)})},update:(id,d)=>apiRequest(`/sessions/${id}`,{method:"PUT",body:JSON.stringify(d)}),delete:id=>apiRequest(`/sessions/${id}`,{method:"DELETE"})};
