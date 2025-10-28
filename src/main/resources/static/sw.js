self.addEventListener('push', function(event) {
    let data = { title: 'Todo Reminder', body: 'You have a task due!' };

    try {
        if (event.data) {
            data = event.data.json();
        }
    } catch (err) {
        console.error('Failed to parse push event data, using default notification', err);
    }

    const options = {
        body: data.body || 'You have a task due!',
        icon: '/icons/todo.png',
        badge: '/icons/todo-badge.png', // optional
        vibrate: [200, 100, 200],       // optional vibration pattern
        data: { url: '/' }               // optional: click redirect
    };

    event.waitUntil(
        self.registration.showNotification(data.title || 'Todo Reminder', options)
    );
});

// Optional: handle notification click
self.addEventListener('notificationclick', function(event) {
    event.notification.close();
    event.waitUntil(
        clients.matchAll({ type: 'window', includeUncontrolled: true }).then(clientList => {
            for (const client of clientList) {
                if ('focus' in client) return client.focus();
            }
            if (clients.openWindow) return clients.openWindow('/');
        })
    );
});
