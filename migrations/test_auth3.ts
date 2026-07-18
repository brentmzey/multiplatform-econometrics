import PocketBase from 'pocketbase';
const pb = new PocketBase('https://polling-data.pockethost.io');
try {
  await pb.collection('_superusers').authWithPassword(process.env.POCKETHOST_ADMIN_EMAIL, process.env.POCKETHOST_ADMIN_PASSWORD);
  console.log('_superusers success');
} catch(e) {
  console.error('_superusers Error:', e.status);
}
