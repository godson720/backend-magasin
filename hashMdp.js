const bcrypt = require('bcryptjs');
bcrypt.hash('Admin1234!', 10).then(h => {
  console.log(h);
  process.exit();
});