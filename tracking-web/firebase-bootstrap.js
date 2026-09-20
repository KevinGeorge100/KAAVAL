/* Hosting supplies public Firebase configuration. Local config is ignored by Git. */
window.firebaseReady = (async () => {
    if (typeof firebase === 'undefined') return;
    const hosted = /\.(web\.app|firebaseapp\.com)$/.test(location.hostname);
    let config = window.__FIREBASE_CONFIG__;
    if (hosted) {
        const response = await fetch('/__/firebase/init.json');
        if (!response.ok) throw new Error('Hosting configuration unavailable');
        config = await response.json();
    } else if (!config) {
        await new Promise(resolve => {
            const script = document.createElement('script');
            script.src = '/firebase-config.js';
            script.onload = resolve;
            script.onerror = resolve;
            document.head.appendChild(script);
        });
        config = window.__FIREBASE_CONFIG__;
    }
    if (config && config.apiKey && !config.apiKey.startsWith('YOUR_') && !firebase.apps.length) {
        firebase.initializeApp(config);
    }
})();
// Initialization errors are surfaced by the page, never as unhandled rejections.
window.firebaseReady.catch(() => {});
