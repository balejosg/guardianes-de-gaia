import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/api_response_model.dart';
import '../models/card_model.dart';
import '../models/card_scan_result_model.dart';
import '../models/collected_card_model.dart';
import '../models/collection_statistics_model.dart';
import '../../domain/entities/card.dart';

abstract class CardRemoteDataSource {
  Future<CardScanResultModel> scanQRCode(int guardianId, String qrCode);
  Future<List<CollectedCardModel>> getGuardianCards(
    int guardianId, {
    CardElement? element,
    CardRarity? rarity,
  });
  Future<CollectionStatisticsModel> getCollectionStatistics(int guardianId);
  Future<List<CollectedCardModel>> getRecentlyCollected(
      int guardianId, int limit);
  Future<CollectedCardModel?> getRarestCard(int guardianId);
  Future<List<CardModel>> searchCards({
    String? name,
    CardElement? element,
    CardRarity? rarity,
  });
  Future<CardModel?> getCard(int cardId);
}

class CardRemoteDataSourceImpl implements CardRemoteDataSource {
  final http.Client client;
  final String baseUrl;

  CardRemoteDataSourceImpl({
    required this.client,
    this.baseUrl = 'http://dev-guardianes.duckdns.org:8080',
  });

  @override
  Future<CardScanResultModel> scanQRCode(int guardianId, String qrCode) async {
    final response = await client.post(
      Uri.parse('$baseUrl/api/v1/guardians/$guardianId/cards/scan'),
      headers: {'Content-Type': 'application/json'},
      body: json.encode({'qrCode': qrCode}),
    );

    if (response.statusCode == 200) {
      // New endpoint returns CardScanResult directly, not wrapped in ApiResponse
      return CardScanResultModel.fromJson(json.decode(response.body));
    } else if (response.statusCode == 400 || response.statusCode == 404) {
      // Handle error responses from new endpoint
      final errorResult =
          CardScanResultModel.fromJson(json.decode(response.body));
      throw Exception(errorResult.message);
    } else {
      throw Exception('Failed to scan QR code: ${response.statusCode}');
    }
  }

  @override
  Future<List<CollectedCardModel>> getGuardianCards(
    int guardianId, {
    CardElement? element,
    CardRarity? rarity,
  }) async {
    final response = await client.get(
      Uri.parse('$baseUrl/api/v1/guardians/$guardianId/cards'),
      headers: {'Content-Type': 'application/json'},
    );

    if (response.statusCode == 200) {
      // New endpoint returns List<CollectedCard> directly, not wrapped in ApiResponse
      final List<dynamic> cardList = json.decode(response.body);
      return cardList
          .map((cardJson) => CollectedCardModel.fromJson(cardJson))
          .toList();
    } else {
      throw Exception('Failed to get guardian cards: ${response.statusCode}');
    }
  }

  @override
  Future<CollectionStatisticsModel> getCollectionStatistics(
      int guardianId) async {
    final response = await client.get(
      Uri.parse('$baseUrl/api/cards/collection/$guardianId/stats'),
      headers: {'Content-Type': 'application/json'},
    );

    if (response.statusCode == 200) {
      final apiResponse = ApiResponseModel<Map<String, dynamic>>.fromJson(
        json.decode(response.body),
        (json) => json as Map<String, dynamic>,
      );

      if (apiResponse.success && apiResponse.data != null) {
        return CollectionStatisticsModel.fromJson(apiResponse.data!);
      } else {
        throw Exception(apiResponse.message);
      }
    } else {
      throw Exception(
          'Failed to get collection statistics: ${response.statusCode}');
    }
  }

  @override
  Future<List<CollectedCardModel>> getRecentlyCollected(
      int guardianId, int limit) async {
    final response = await client.get(
      Uri.parse(
          '$baseUrl/api/cards/collection/$guardianId/recent?limit=$limit'),
      headers: {'Content-Type': 'application/json'},
    );

    if (response.statusCode == 200) {
      final apiResponse = ApiResponseModel<List<dynamic>>.fromJson(
        json.decode(response.body),
        (json) => json as List<dynamic>,
      );

      if (apiResponse.success && apiResponse.data != null) {
        return apiResponse.data!
            .map((cardJson) => CollectedCardModel.fromJson(cardJson))
            .toList();
      } else {
        throw Exception(apiResponse.message);
      }
    } else {
      throw Exception(
          'Failed to get recently collected cards: ${response.statusCode}');
    }
  }

  @override
  Future<CollectedCardModel?> getRarestCard(int guardianId) async {
    final response = await client.get(
      Uri.parse('$baseUrl/api/cards/collection/$guardianId/rarest'),
      headers: {'Content-Type': 'application/json'},
    );

    if (response.statusCode == 200) {
      final apiResponse = ApiResponseModel<Map<String, dynamic>?>.fromJson(
        json.decode(response.body),
        (json) => json as Map<String, dynamic>?,
      );

      if (apiResponse.success) {
        return apiResponse.data != null
            ? CollectedCardModel.fromJson(apiResponse.data!)
            : null;
      } else {
        throw Exception(apiResponse.message);
      }
    } else {
      throw Exception('Failed to get rarest card: ${response.statusCode}');
    }
  }

  @override
  Future<List<CardModel>> searchCards({
    String? name,
    CardElement? element,
    CardRarity? rarity,
  }) async {
    final uri = Uri.parse('$baseUrl/api/cards/search');
    final params = <String, String>{};

    if (name != null && name.isNotEmpty) {
      params['name'] = name;
    }
    if (element != null) {
      params['element'] = element.name.toUpperCase();
    }
    if (rarity != null) {
      params['rarity'] = rarity.name.toUpperCase();
    }

    final response = await client.get(
      uri.replace(queryParameters: params.isNotEmpty ? params : null),
      headers: {'Content-Type': 'application/json'},
    );

    if (response.statusCode == 200) {
      final apiResponse = ApiResponseModel<List<dynamic>>.fromJson(
        json.decode(response.body),
        (json) => json as List<dynamic>,
      );

      if (apiResponse.success && apiResponse.data != null) {
        return apiResponse.data!
            .map((cardJson) => CardModel.fromJson(cardJson))
            .toList();
      } else {
        throw Exception(apiResponse.message);
      }
    } else {
      throw Exception('Failed to search cards: ${response.statusCode}');
    }
  }

  @override
  Future<CardModel?> getCard(int cardId) async {
    final response = await client.get(
      Uri.parse('$baseUrl/api/cards/$cardId'),
      headers: {'Content-Type': 'application/json'},
    );

    if (response.statusCode == 200) {
      final apiResponse = ApiResponseModel<Map<String, dynamic>>.fromJson(
        json.decode(response.body),
        (json) => json as Map<String, dynamic>,
      );

      if (apiResponse.success && apiResponse.data != null) {
        return CardModel.fromJson(apiResponse.data!);
      } else {
        throw Exception(apiResponse.message);
      }
    } else if (response.statusCode == 404) {
      return null;
    } else {
      throw Exception('Failed to get card: ${response.statusCode}');
    }
  }
}
