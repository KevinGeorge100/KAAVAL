const fs=require('node:fs');
const {initializeTestEnvironment,assertSucceeds,assertFails}=require('@firebase/rules-unit-testing');
const {doc,setDoc,getDoc,getDocs,collection,updateDoc,deleteDoc,Timestamp}=require('firebase/firestore');
(async()=>{
const env=await initializeTestEnvironment({projectId:'demo-kaaval-audit',firestore:{host:'127.0.0.1',port:8081,rules:fs.readFileSync('firestore.rules','utf8')}});
const owner=env.authenticatedContext('owner').firestore(), stranger=env.authenticatedContext('stranger').firestore(), caregiver=env.authenticatedContext('caregiver').firestore(), anonymous=env.unauthenticatedContext().firestore();
const ref=db=>doc(db,'incidents','KVL-audit');const data={incidentId:'KVL-audit',ownerUid:'owner',status:'ACTIVE',createdAt:Timestamp.now(),expiresAt:Timestamp.fromMillis(Date.now()+3600000),userName:'Test user'};
let passed=0;const check=async(name,fn)=>{await fn();passed++;console.log('PASS '+name);};
try {
await env.clearFirestore();
await check('missing document can be observed',()=>assertSucceeds(getDoc(ref(anonymous))));
await check('unauthenticated creation denied',()=>assertFails(setDoc(ref(anonymous),data)));
await check('forged ownership denied',()=>assertFails(setDoc(ref(stranger),data)));
await check('owner can create',()=>assertSucceeds(setDoc(ref(owner),data)));
await check('active link can be read',()=>assertSucceeds(getDoc(ref(anonymous))));
await check('enumeration denied',()=>assertFails(getDocs(collection(anonymous,'incidents'))));
await check('anonymous location write denied',()=>assertFails(updateDoc(ref(anonymous),{latitude:1})));
await check('stranger location write denied',()=>assertFails(updateDoc(ref(stranger),{latitude:1})));
await check('owner location write allowed',()=>assertSucceeds(updateDoc(ref(owner),{latitude:0,longitude:0})));
await check('owner cannot grant caregiver access',()=>assertFails(updateDoc(ref(owner),{caregiverUids:['stranger']})));
await check('owner cannot extend expiry',()=>assertFails(updateDoc(ref(owner),{expiresAt:Timestamp.fromMillis(Date.now()+7200000)})));
await env.withSecurityRulesDisabled(async ctx=>updateDoc(ref(ctx.firestore()),{caregiverUids:['caregiver']}));
await check('authorized caregiver reassurance allowed',()=>assertSucceeds(updateDoc(ref(caregiver),{reassurancePing:Timestamp.now()})));
await check('caregiver cannot forge GPS',()=>assertFails(updateDoc(ref(caregiver),{latitude:2})));
await check('legacy unauthenticated responses denied',()=>assertFails(setDoc(doc(anonymous,'incidents/KVL-audit/responses/x'),{caregiverName:'Attacker'})));
await check('owner can complete',()=>assertSucceeds(updateDoc(ref(owner),{status:'COMPLETED'})));
await check('completed incident cannot reopen',()=>assertFails(updateDoc(ref(owner),{status:'ACTIVE'})));
await check('delete denied',()=>assertFails(deleteDoc(ref(owner))));
await env.withSecurityRulesDisabled(async ctx=>updateDoc(ref(ctx.firestore()),{expiresAt:Timestamp.fromMillis(Date.now()-1000)}));
await check('expired public read denied',()=>assertFails(getDoc(ref(anonymous))));
await check('expired owner read allowed',()=>assertSucceeds(getDoc(ref(owner))));
console.log(`${passed} security tests passed`);
} finally {await env.cleanup();}
})().catch(e=>{console.error(e);process.exitCode=1;});
