import React, { useState, useEffect } from 'react';
import { commentService, activityService, attachmentService } from '../services/api';

const CaseDetail = ({ caseData, onBack, onEdit, currentUserId }) => {
    const [newMessage, setNewMessage] = useState('');
    const [timeline, setTimeline] = useState([]);
    const [attachments, setAttachments] = useState([]);
    const [isUploading, setIsUploading] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchAllData = async () => {
            if (!caseData?.id) return;
            try {
                setLoading(true);
                // Hämta kommentarer, loggar och bilagor parallellt
                const [commentsRes, logsRes, attachRes] = await Promise.all([
                    commentService.getByRecord(caseData.id),
                    activityService.getLogsByRecord(caseData.id, currentUserId),
                    attachmentService.getByRecord(caseData.id)
                ]);

                // Kombinera och sortera tidslinjen (Kommentarer + Loggar)
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

    // --- LOGIK FÖR MEDDELANDEN ---
    const handleSendMessage = async () => {
        if (!newMessage.trim()) return;
        try {
            const res = await commentService.createComment({
                recordId: caseData.id,
                body: newMessage
            });
            // Lägg till nya kommentaren i tidslinjen lokalt för omedelbar respons
            setTimeline(prev => [...prev, { ...res.data, type: 'COMMENT' }]);
            setNewMessage('');
        } catch (error) {
            alert("Kunde inte skicka meddelande.");
        }
    };

    // --- LOGIK FÖR FILUPPLADDNING (S3) ---
    const handleFileUpload = async (event) => {
        const file = event.target.files[0];
        if (!file) return;

        // Validering baserad på din AttachmentPolicy
        const allowedTypes = ['image/jpeg', 'image/png', 'application/pdf'];
        if (!allowedTypes.includes(file.type)) {
            alert("Endast JPG, PNG och PDF är tillåtna.");
            return;
        }
        if (file.size > 10 * 1024 * 1024) {
            alert("Filen är för stor (max 10MB).");
            return;
        }

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
            alert("Du har inte behörighet att radera denna fil.");
        }
    };

    if (!caseData) return <div className="p-10 text-center italic">Laddar ärende...</div>;

    return (
        <div className="max-w-6xl mx-auto space-y-8 animate-in fade-in slide-in-from-right-4 duration-500 pb-20">

            {/* TILLBAKA-KNAPP */}
            <button
                onClick={onBack}
                className="flex items-center gap-2 text-slate-400 hover:text-vet-navy font-bold text-[10px] uppercase tracking-[0.2em] transition group"
            >
                <span className="group-hover:-translate-x-1 transition-transform">←</span> Tillbaka
            </button>

            {/* HEADER CARD */}
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                <div className="flex items-center gap-5">
                    <div className="bg-vet-navy text-white h-12 w-12 rounded-lg flex items-center justify-center text-xl font-bold shadow-lg shadow-vet-navy/10 italic">
                        {caseData.petName?.charAt(0) || "#"}
                    </div>
                    <div>
                        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">{caseData.title}</h1>
                        <div className="flex items-center gap-3 mt-2">
                            <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase border italic ${
                                caseData.status === 'OPEN' ? 'bg-green-50 text-green-700 border-green-100' : 'bg-slate-50 text-slate-500 border-slate-100'
                            }`}>
                                {caseData.status}
                            </span>
                            <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider italic">Patient: {caseData.petName}</span>
                        </div>
                    </div>
                </div>
                <div className="flex gap-2">
                    <button onClick={() => onEdit(caseData)} className="bg-slate-50 text-slate-600 px-4 py-2 rounded-lg text-xs font-bold border border-slate-200 hover:bg-slate-100 transition">
                        Redigera beskrivning
                    </button>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

                {/* VÄNSTERKOLUMN: INFO, BILAGOR & CHATT */}
                <div className="lg:col-span-2 space-y-6">

                    {/* BESKRIVNING */}
                    <section className="bg-white p-8 rounded-xl border border-slate-200 shadow-sm">
                        <h2 className="text-[10px] font-bold text-vet-accent uppercase tracking-widest mb-4 italic">Sjukdomshistorik & Observationer</h2>
                        <p className="text-slate-600 leading-relaxed font-medium italic">
                            {caseData.description}
                        </p>
                    </section>

                    {/* BILAGOR (S3) */}
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
                                        {file.fileType?.includes('image') ? (
                                            <img src={file.downloadUrl} alt={file.fileName} className="w-full h-full object-cover" />
                                        ) : (
                                            <div className="text-vet-navy font-bold text-[10px] uppercase">PDF</div>
                                        )}
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
                            {attachments.length === 0 && (
                                <p className="col-span-full text-center py-4 text-slate-400 text-xs italic">Inga bilagor uppladdade.</p>
                            )}
                        </div>
                    </section>

                    {/* KONVERSATION */}
                    <section className="space-y-4">
                        <h2 className="text-sm font-bold text-slate-800 ml-1 uppercase tracking-widest italic">Konversation</h2>
                        <div className="space-y-4 max-h-[500px] overflow-y-auto pr-2">
                            {timeline.filter(item => item.type === 'COMMENT').map((comment) => (
                                <div key={comment.id} className={`flex gap-3 items-start ${comment.authorId === currentUserId ? 'flex-row-reverse' : ''}`}>
                                    <div className={`h-8 w-8 rounded-lg flex-shrink-0 flex items-center justify-center text-white text-[10px] font-bold shadow-sm italic ${
                                        comment.authorId === currentUserId ? 'bg-vet-accent' : 'bg-vet-navy'
                                    }`}>
                                        {comment.authorName?.charAt(0)}
                                    </div>
                                    <div className={`p-4 rounded-xl border shadow-sm max-w-[85%] ${
                                        comment.authorId === currentUserId
                                            ? 'bg-blue-50 border-blue-100 rounded-tr-none text-right'
                                            : 'bg-white border-slate-200 rounded-tl-none'
                                    }`}>
                                        <p className="text-[10px] font-bold text-vet-accent italic uppercase tracking-tighter">{comment.authorName}</p>
                                        <p className="text-sm text-slate-600 mt-1 font-medium italic">{comment.body}</p>
                                        <p className="text-[9px] text-slate-300 mt-2 font-bold uppercase">
                                            {new Date(comment.createdAt).toLocaleString('sv-SE', { timeStyle: 'short', dateStyle: 'short'})}
                                        </p>
                                    </div>
                                </div>
                            ))}
                        </div>

                        {/* INPUT FÖR NYTT MEDDELANDE */}
                        <div className="bg-white p-2 rounded-xl border border-slate-200 shadow-sm flex gap-2 focus-within:border-vet-accent transition">
                            <input
                                type="text"
                                value={newMessage}
                                onChange={(e) => setNewMessage(e.target.value)}
                                onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                                placeholder="Skriv ett meddelande till kliniken..."
                                className="flex-1 bg-transparent px-4 text-sm outline-none italic"
                            />
                            <button
                                onClick={handleSendMessage}
                                className="bg-vet-navy text-white px-5 py-2 rounded-lg hover:bg-vet-accent transition font-bold text-xs uppercase tracking-widest"
                            >
                                Skicka
                            </button>
                        </div>
                    </section>
                </div>

                {/* HÖGERKOLUMN: PATIENTKORT & LOGG */}
                <div className="space-y-6">
                    <div className="bg-vet-navy p-6 rounded-xl text-white shadow-xl shadow-vet-navy/10 border border-white/5">
                        <h3 className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-4 italic">Patientkort</h3>
                        <div className="flex items-center gap-4">
                            <div className="text-3xl bg-white/10 p-3 rounded-lg">🐕</div>
                            <div>
                                <p className="font-bold text-lg italic leading-none">{caseData.petName}</p>
                                <p className="text-xs text-slate-400 mt-1 uppercase font-bold tracking-tighter italic">
                                    {caseData.petSpecies} • {caseData.clinicName}
                                </p>
                            </div>
                        </div>
                    </div>

                    <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
                        <h3 className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-6 border-b border-slate-50 pb-2 italic">Händelselogg</h3>
                        <div className="space-y-6 relative before:absolute before:left-3 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-100">
                            {timeline.filter(item => item.type === 'ACTIVITY').map((log) => (
                                <div key={log.id} className="relative pl-8">
                                    <div className="absolute left-1.5 top-1.5 w-3 h-3 rounded-full bg-slate-300 border-2 border-white ring-4 ring-slate-50"></div>
                                    <p className="text-[11px] font-bold text-slate-800 italic leading-tight">{log.description}</p>
                                    <p className="text-[9px] text-slate-400 font-bold uppercase mt-1 italic tracking-tighter">
                                        {new Date(log.createdAt).toLocaleString('sv-SE')}
                                    </p>
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