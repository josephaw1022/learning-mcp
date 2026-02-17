const { Client } = require('pg');
const { faker } = require('@faker-js/faker');
const crypto = require('crypto');

const client = new Client({
  user: 'postgres',
  host: 'localhost',
  database: 'quarkus_db',
  password: 'postgres',
  port: 5432,
});

async function seed() {
  await client.connect();
  console.log('Connected to database');

  const schemas = ['evidence', 'audit', 'users', 'storage'];
  for (const s of schemas) {
    await client.query(`CREATE SCHEMA IF NOT EXISTS ${s};`);
  }

  // 1. Seed Departments
  console.log('Seeding Departments...');
  const departments = [];
  for (let i = 0; i < 10; i++) {
    const deptName = faker.company.name() + " Division";
    departments.push(deptName);
    await client.query(
      'INSERT INTO users.Department (id, name, location, contactEmail) VALUES (nextval(\'users.department_seq\'), $1, $2, $3)',
      [deptName, faker.location.city(), faker.internet.email()]
    );
  }

  // 2. Seed Officers
  console.log('Seeding Officers...');
  const officers = [];
  for (let i = 0; i < 200; i++) {
    const badge = faker.string.alphanumeric(6).toUpperCase();
    officers.push(badge);
    await client.query(
      'INSERT INTO users.Officer (badgeNumber, fullName, departmentName, role) VALUES ($1, $2, $3, $4) ON CONFLICT (badgeNumber) DO NOTHING',
      [badge, faker.person.fullName(), faker.helpers.arrayElement(departments), faker.helpers.arrayElement(['ADMIN', 'TECHNICIAN', 'OFFICER', 'LEGAL_VIEWER'])]
    );
  }

  // 3. Seed User Sessions
  console.log('Seeding User Sessions...');
  for (let i = 0; i < 1000; i++) {
    await client.query(
      'INSERT INTO users.UserSession (id, badgeNumber, loginTime, logoutTime, ipAddress) VALUES (nextval(\'users.usersession_seq\'), $1, $2, $3, $4)',
      [faker.helpers.arrayElement(officers), faker.date.recent(), faker.date.recent(), faker.internet.ip()]
    );
  }

  // 4. Seed Storage Facilities and Locations
  console.log('Seeding Storage Facilities and Locations...');
  const facilities = ['FAC-001', 'FAC-002', 'FAC-003', 'FAC-004', 'FAC-005'];
  const locationIds = [];
  for (const f of facilities) {
    await client.query(
      'INSERT INTO storage.StorageFacility (facilityId, name, address, isActive) VALUES ($1, $2, $3, $4) ON CONFLICT (facilityId) DO NOTHING',
      [f, faker.company.name() + ' Facility', faker.location.streetAddress(), true]
    );

    for (let j = 0; j < 10; j++) {
      const res = await client.query(
        'INSERT INTO storage.StorageLocation (id, label, type, capacity, facilityId) VALUES (nextval(\'storage.storagelocation_seq\'), $1, $2, $3, $4) RETURNING id',
        [f + '-LOC-' + j, faker.helpers.arrayElement(['PHYSICAL_LOCKER', 'REFRIGERATED', 'CLOUD_S3', 'OFF_SITE_VAULT']), faker.number.int({min: 10, max: 100}), f]
      );
      locationIds.push(res.rows[0].id);
    }
  }

  // 5. Seed Storage Maintenance
  console.log('Seeding Storage Maintenance...');
  for (let i = 0; i < 100; i++) {
    await client.query(
      'INSERT INTO storage.StorageMaintenance (id, locationId, checkTime, performedBy, results) VALUES (nextval(\'storage.storagemaintenance_seq\'), $1, $2, $3, $4)',
      [faker.helpers.arrayElement(locationIds), faker.date.past(), faker.helpers.arrayElement(officers), 'Inspection passed: ' + faker.lorem.sentence()]
    );
  }

  // 6. Seed Tags
  console.log('Seeding Evidence Tags...');
  for (let i = 0; i < 20; i++) {
    await client.query(
      'INSERT INTO evidence.EvidenceTag (id, tagName, colorCode) VALUES (nextval(\'evidence.evidencetag_seq\'), $1, $2)',
      [faker.lorem.word(), faker.color.rgb()]
    );
  }

  // 7. Massive Case and Evidence Seeding
  const TOTAL_CASES = 500;
  const EVIDENCE_PER_CASE = 20;
  console.log(`Starting massive seeding: ${TOTAL_CASES} cases, ~${TOTAL_CASES * EVIDENCE_PER_CASE} items...`);

  for (let c = 0; c < TOTAL_CASES; c++) {
    const caseId = `CASE-${faker.string.alphanumeric(6).toUpperCase()}`;
    const officerId = faker.helpers.arrayElement(officers);
    
    await client.query(
      'INSERT INTO evidence.CaseMetadata (caseId, title, description, openedAt, priority) VALUES ($1, $2, $3, $4, $5) ON CONFLICT (caseId) DO NOTHING',
      [caseId, faker.lorem.sentence(), faker.lorem.paragraph(), faker.date.past(), faker.helpers.arrayElement(['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'])]
    );

    for (let e = 0; e < EVIDENCE_PER_CASE; e++) {
      const evidenceId = faker.string.uuid();
      const filename = faker.system.fileName();
      
      await client.query('INSERT INTO evidence.EvidenceItem (uuid, caseId, filename, mimeType, fileSizeBytes, sha256Hash, storageProviderRef, status) VALUES ($1, $2, $3, $4, $5, $6, $7, $8)', 
        [evidenceId, caseId, filename, 'application/octet-stream', faker.number.int({ min: 1000, max: 10000000 }), crypto.randomBytes(32).toString('hex'), `storage/${evidenceId}`, 'INGESTED']);

      await client.query('INSERT INTO audit.AuditEntry (id, timestamp, actorId, evidenceId, actionType, notes) VALUES (nextval(\'public.auditentry_seq\'), $1, $2, $3, $4, $5)', 
        [new Date(), officerId, evidenceId, 'UPLOAD', 'Seeded via mass script']);

      await client.query('INSERT INTO evidence.Custodian (id, evidenceId, userId, assignedAt, notes) VALUES (nextval(\'public.custodian_seq\'), $1, $2, $3, $4)', 
        [evidenceId, officerId, new Date(), 'Initial assignment']);

      if (faker.datatype.boolean()) {
        await client.query('INSERT INTO evidence.EvidenceNote (id, evidenceId, authorId, noteText) VALUES (nextval(\'evidence.evidencenote_seq\'), $1, $2, $3)',
          [evidenceId, officerId, faker.lorem.sentence()]);
      }
    }
    if (c % 100 === 0) console.log(`Progress: ${c} cases processed...`);
  }

  // 8. Seed Access Logs
  console.log('Seeding Access Logs...');
  for (let i = 0; i < 5000; i++) {
    await client.query('INSERT INTO audit.AccessLog (id, userId, action, timestamp, success) VALUES (nextval(\'audit.accesslog_seq\'), $1, $2, $3, $4)',
      [faker.helpers.arrayElement(officers), faker.helpers.arrayElement(['LOGIN', 'LOGOUT', 'DOWNLOAD', 'VERIFY', 'TRANSFER']), faker.date.recent(), faker.datatype.boolean()]);
  }

  // 9. Seed System Alerts
  console.log('Seeding System Alerts...');
  for (let i = 0; i < 500; i++) {
    await client.query('INSERT INTO audit.SystemAlert (id, severity, message, alertTime, isResolved) VALUES (nextval(\'audit.systemalert_seq\'), $1, $2, $3, $4)',
      [faker.helpers.arrayElement(['INFO', 'WARNING', 'CRITICAL']), 'Alert: ' + faker.lorem.sentence(), faker.date.recent(), faker.datatype.boolean()]);
  }

  console.log('Full database seeding complete');
  await client.end();
}

seed().catch(err => {
  console.error('Error during seeding:', err);
  process.exit(1);
});
