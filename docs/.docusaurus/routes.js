import React from 'react';
import ComponentCreator from '@docusaurus/ComponentCreator';

export default [
  {
    path: '/es-419/docs',
    component: ComponentCreator('/es-419/docs', 'd69'),
    routes: [
      {
        path: '/es-419/docs',
        component: ComponentCreator('/es-419/docs', '577'),
        routes: [
          {
            path: '/es-419/docs',
            component: ComponentCreator('/es-419/docs', 'f0f'),
            routes: [
              {
                path: '/es-419/docs/architecture',
                component: ComponentCreator('/es-419/docs/architecture', '2f8'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/es-419/docs/developers',
                component: ComponentCreator('/es-419/docs/developers', 'e50'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/es-419/docs/intro',
                component: ComponentCreator('/es-419/docs/intro', 'af2'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/es-419/docs/roadmap',
                component: ComponentCreator('/es-419/docs/roadmap', '730'),
                exact: true,
                sidebar: "docs"
              },
              {
                path: '/es-419/docs/team',
                component: ComponentCreator('/es-419/docs/team', 'd77'),
                exact: true,
                sidebar: "docs"
              }
            ]
          }
        ]
      }
    ]
  },
  {
    path: '/es-419/',
    component: ComponentCreator('/es-419/', '7a5'),
    exact: true
  },
  {
    path: '*',
    component: ComponentCreator('*'),
  },
];
