// import proj4 from 'proj4'

// export const transform = (point, srsToCode) => {
//   if (!point) return null

//   const { srs: srsFromCode } = point

//   if (srsFromCode === srsToCode) {
//     // projection is not needed
//     return point
//   }
//   const srsFrom = srsIndex[srsFrom]
//   if (!srsFrom) {
//     // invalid srs specified in point
//     return null
//   }

//   const srsTo = srsIndex[srsCodeTo]
//   if (!srsTo) {
//     // invalid target srs code
//     return null
//   }
//   try {
//     const { x, y } = point
//     const [long, lat] = proj4(srsFrom.wkt, srsTo.wkt, [Number(x), Number(y)])

//     return { srs: srsCodeTo, x: long, y: lat }
//   } catch (error) {
//     return null
//   }
// }

// export const GeoUtils = {
//   transform,
// }
