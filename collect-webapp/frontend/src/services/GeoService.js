import AbstractService from './AbstractService'

export default class GeoService extends AbstractService {
  convertCoordinate(coordinate, srsIdTo) {
    const { x, y, srs } = coordinate
    return this.get('geo/coordinate/convert.json', { x, y, srs, srsIdTo })
  }
}
