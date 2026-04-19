import React, { useState, useEffect, useRef } from 'react';
import { commentService, activityService, attachmentService } from '../services/api';
import { STATUS_MAP } from '../utils/statusHelper';

const CaseDetail = ({ caseData, onBack, onGoToPet, currentUserId }) => {
    const [newMessage, setNewMessage] = useState('');
    const [timeline, setTimeline] = useState([]);
    const [attachments, setAttachments] = useState([]);
    const [isUploading, setIsUploading] = useState(false);
    const [loading, setLoading] = useState(true);

    const messagesEndRef = useRef(null);

    // Hämta svensk konfiguration för status
    const statusConfig = STATUS_MAP[caseData?.status] || { label: caseData?.status, color: 'bg-slate-50 text-slate-500 border-slate-100' };

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        if (timeline.length > 0) {
            scrollToBottom();
        }
    }, [timeline]);

    useEffect(() => {
        const fetchAllData = async () => {
            if (!caseData?.id) return;
            try {
                setLoading(true);
                const [commentsRes, logsRes, attachRes] = await Promise.all([
                    commentService.getByRecord(caseData.id),
                    activityService.getLogsByRecord(caseData.id, currentUserId),
                    attachmentService.getByRecord(caseData.id)
                ]);

                const combinedTimeline = [
                    ...commentsRes.data.map(c => ({ ...c, type: 'COMMENT' })),
                    ...logsRes.data.map(l => ({ ...l, type: 'ACTIVITY' }))
                ].sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));

                setTimeline(combinedTimeline);
                setAttachments(attachRes.data);
            } catch (error) {
                console.error("Fel vid hämtning av data:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchAllData();
    }, [caseData?.id, currentUserId]);

    const handleSendMessage = async () => {
        if (!newMessage.trim()) return;
        try {
            const res = await commentService.createComment({
                recordId: caseData.id,
                body: newMessage
            });
            setTimeline(prev => [...prev, { ...res.data, type: 'COMMENT' }]);
            setNewMessage('');
        } catch (error) {
            alert("Kunde inte skicka meddelande.");
        }
    };

    const handleFileUpload = async (event) => {
        const file = event.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append('file', file);
        formData.append('description', 'Uppladdad via portalen');

        try {
            setIsUploading(true);
            const res = await attachmentService.upload(caseData.id, formData);
            setAttachments(prev => [...prev, res.data]);
        } catch (error) {
            alert("Kunde inte ladda upp filen.");
        } finally {
            setIsUploading(false);
        }
    };

    const handleDeleteAttachment = async (id) => {
        if (!window.confirm("Vill du radera bilagan?")) return;
        try {
            await attachmentService.delete(id);
            setAttachments(prev => prev.filter(a => a.id !== id));
        } catch (error) {
            alert("Kunde inte radera filen.");
        }
    };

    if (!caseData) return <div className="p-10 text-center italic">Laddar ärende...</div>;

    return (
        <div className="max-w-6xl mx-auto space-y-8 animate-in fade-in slide-in-from-right-4 duration-500 pb-20">
            <button
                onClick={onBack}
                className="flex items-center gap-2 text-slate-400 hover:text-vet-navy font-bold text-[10px] uppercase tracking-[0.2em] transition group"
            >
                <span className="group-hover:-translate-x-1 transition-transform">←</span> Tillbaka
            </button>

            {/* HEADER CARD - Uppdaterad med svensk status */}
            <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                <div className="flex items-center gap-5">
                    <div className="bg-vet-navy text-white h-12 w-12 rounded-lg flex items-center justify-center text-xl font-bold shadow-lg shadow-vet-navy/10 italic">
                        {caseData.petName?.charAt(0) || "#"}
                    </div>
                    <div>
                        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">{caseData.title}</h1>
                        <div className="flex items-center gap-3 mt-2">
                            <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase border italic ${statusConfig.color}`}>
                                {statusConfig.label}
                            </span>
                            <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider italic">Patient: {caseData.petName}</span>
                        </div>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2 space-y-6">
                    <section className="bg-white p-8 rounded-xl border border-slate-200 shadow-sm">
                        <h2 className="text-[10px] font-bold text-vet-accent uppercase tracking-widest mb-4 italic">Sjukdomshistorik & Observationer</h2>
                        <p className="text-slate-600 leading-relaxed font-medium italic">{caseData.description}</p>
                    </section>

                    <section className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                        <div className="flex justify-between items-center mb-6">
                            <h3 className="text-[10px] font-bold text-slate-400 uppercase tracking-widest italic">Medicinska Bilagor</h3>
                            <label className={`cursor-pointer bg-vet-navy text-white text-[10px] font-bold px-4 py-2 rounded-lg transition uppercase tracking-widest ${isUploading ? 'opacity-50 animate-pulse' : 'hover:bg-vet-accent'}`}>
                                {isUploading ? 'Laddar upp...' : '+ Ladda upp'}
                                <input type="file" className="hidden" onChange={handleFileUpload} disabled={isUploading} />
                            </label>
                        </div>
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                            {attachments.map((file) => (
                                <div key={file.id} className="group relative bg-slate-50 rounded-xl border border-slate-200 overflow-hidden shadow-sm">
                                    <div className="aspect-square flex items-center justify-center bg-slate-100">
                                        {file.fileType?.includes('image') ? <img src={file.downloadUrl} alt={file.fileName} className="w-full h-full object-cover" /> : <div className="text-vet-navy font-bold text-[10px] uppercase">PDF</div>}
                                    </div>
                                    <div className="p-2 bg-white border-t border-slate-100">
                                        <p className="text-[9px] font-bold text-slate-700 truncate italic">{file.fileName}</p>
                                        <div className="flex justify-between items-center mt-1">
                                            <a href={file.downloadUrl} target="_blank" rel="noreferrer" className="text-[9px] text-blue-600 font-bold uppercase hover:underline">Visa</a>
                                            <button onClick={() => handleDeleteAttachment(file.id)} className="text-[9px] text-red-400 font-bold uppercase hover:text-red-600">Radera</button>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </section>

                    <section className="space-y-4">
                        <h2 className="text-sm font-bold text-slate-800 ml-1 uppercase tracking-widest italic">Konversation</h2>
                        <div className="bg-slate-50/50 p-4 rounded-2xl border border-slate-100 shadow-inner">
                            <div className="space-y-4 max-h-[500px] overflow-y-auto pr-2 custom-scrollbar">
                                {timeline.filter(item => item.type === 'COMMENT').map((comment) => (
                                    <div key={comment.id} className={`flex gap-3 items-end ${comment.authorId === currentUserId ? 'flex-row-reverse' : 'flex-row'}`}>
                                        <div className={`h-8 w-8 rounded-lg flex-shrink-0 flex items-center justify-center text-white text-[10px] font-bold shadow-sm italic ${comment.authorId === currentUserId ? 'bg-vet-accent' : 'bg-vet-navy'}`}>
                                            {comment.authorName?.charAt(0)}
                                        </div>
                                        <div className={`p-4 rounded-2xl border shadow-sm max-w-[80%] ${comment.authorId === currentUserId ? 'bg-vet-navy text-white border-vet-navy rounded-br-none' : 'bg-white border-slate-200 text-slate-700 rounded-bl-none'}`}>
                                            <div className={`text-[9px] font-black uppercase tracking-tighter mb-1 opacity-70 ${comment.authorId === currentUserId ? 'text-blue-200' : 'text-vet-accent'}`}>{comment.authorId === currentUserId ? 'Du' : comment.authorName}</div>
                                            <p className="text-sm font-medium leading-relaxed italic">{comment.body}</p>
                                        </div>
                                    </div>
                                ))}
                                <div ref={messagesEndRef} />
                            </div>
                        </div>
                        <div className="bg-white p-2 rounded-xl border-2 border-slate-100 shadow-lg flex gap-2 focus-within:border-vet-accent transition-all">
                            <input type="text" value={newMessage} onChange={(e) => setNewMessage(e.target.value)} onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()} placeholder="Skriv ett meddelande..." className="flex-1 bg-transparent px-4 py-3 text-sm outline-none italic font-medium" />
                            <button onClick={handleSendMessage} disabled={!newMessage.trim()} className="bg-vet-navy text-white px-6 py-2 rounded-lg hover:bg-vet-accent transition-all font-bold text-xs uppercase tracking-widest shadow-md disabled:opacity-30">Skicka</button>
                        </div>
                    </section>
                </div>

                <div className="space-y-6">
                    <button onClick={() => onGoToPet(caseData.petId)} className="w-full text-left bg-vet-navy p-6 rounded-xl text-white shadow-xl hover:bg-slate-800 transition-all group border border-white/5">
                        <h3 className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-4 italic flex justify-between">Patientkort <span className="opacity-0 group-hover:opacity-100 transition-opacity">Visa profil →</span></h3>
                        <div className="flex items-center gap-4">
                            <div className="w-12 h-12 bg-white/10 rounded-lg flex items-center justify-center text-xl font-bold italic">{caseData.petName?.charAt(0) || "?"}</div>
                            <div>
                                <p className="font-bold text-lg italic leading-none group-hover:text-vet-accent">{caseData.petName}</p>
                                <p className="text-xs text-slate-400 mt-1 uppercase font-bold italic">{caseData.petSpecies} • {caseData.clinicName}</p>
                            </div>
                        </div>
                    </button>

                    <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                        <h3 className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-6 border-b pb-2 italic">Händelselogg</h3>
                        <div className="space-y-6 relative before:absolute before:left-3 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-100">
                            {timeline.filter(item => item.type === 'ACTIVITY').map((log) => (
                                <div key={log.id} className="relative pl-8">
                                    <div className="absolute left-1.5 top-1.5 w-3 h-3 rounded-full bg-slate-300 border-2 border-white ring-4 ring-slate-50"></div>
                                    <p className="text-[11px] font-bold text-slate-800 italic leading-tight">{log.description}</p>
                                    <p className="text-[9px] text-slate-400 font-bold uppercase mt-1 italic tracking-tighter">{new Date(log.createdAt).toLocaleString('sv-SE')}</p>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CaseDetail;