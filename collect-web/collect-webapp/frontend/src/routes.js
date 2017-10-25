const routes = [
  {
    path: '/',
    name: 'Home'
  },
  {
    path: '/home',
    name: 'Home'
  },
  {
    path: '/signin',
    name: "Signin"
  },
  {
    path: '/dashboard',
    name: "Dashboard"
  },
  {
    path: '/datamanagement',
    name: "Data Management"
  },
  {
    path: '/datamanagement/export',
    name: "Export Data"
  },
  {
    path: '/datamanagement/csvexport',
    name: "Export to CSV"
  },
  {
    path: '/datamanagement/backup',
    name: "Backup data"
  },
  {
    path: '/datamanagement/csvimport',
    name: "Import Data from CSV/Excel"
  },
  {
    path: '/datamanagement/backupimport',
    name: "Import Backup file"
  },
  {
    path: '/datamanagement/:id',
    name: 'Record',
    pathRegExp: new RegExp('/datamanagement/(\\d)+')
  },
  {
    path: '/surveydesigner',
    name: "Survey Designer"
  },
  {
    path: '/datacleansing',
    name: "Data Cleansing"
  },
  {
    path: '/map',
    name: "Map"
  },
  {
    path: '/saiku',
    name: "Saiku"
  },
  {
    path: '/users',
    name: "Users"
  },
  {
    path: '/usergroups',
    name: "User Groups"
  },
  {
    path: '/usergroups/:id',
    name: 'User Group',
    pathRegExp: new RegExp('/usergroups/(\\d)+')
  },
  {
    path: '/usergroups/new',
    name: 'New User Group'
  }
];
export default routes;
