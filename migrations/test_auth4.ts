import PocketBase from 'pocketbase';
const pb = new PocketBase('https://polling-data.pockethost.io');
try {
  await pb.admins.authWithPassword(process.env.POCKETHOST_ADMIN_EMAIL, process.env.POCKETHOST_ADMIN_PASSWORD);
  console.log('Success! admins');
} catch(e) {
  console.error('Error status:', e.status);
  console.error('Error response:', JSON.stringify(e.response, null, 2));
}
