"""
M-Pesa Service
Handles Safaricom M-Pesa integration for Kenya mobile payments
Supports STK Push, payment verification, and webhook callbacks
"""
import axios from 'axios';
import crypto from 'crypto';
import { logger } from '../utils/logger';

interface MPesaConfig {
  consumerKey: string;
  consumerSecret: string;
  businessShortCode: string;
  passkey: string;
  callbackUrl: string;
  environment: 'sandbox' | 'production';
}

interface STKPushRequest {
  phoneNumber: string;
  amount: number;
  accountReference: string;
  transactionDesc: string;
}

interface STKPushResponse {
  MerchantRequestID: string;
  CheckoutRequestID: string;
  ResponseCode: string;
  ResponseDescription: string;
  CustomerMessage: string;
}

export class MPesaService {
  private config: MPesaConfig;
  private baseUrl: string;
  private accessToken: string | null = null;
  private tokenExpiry: number = 0;

  constructor() {
    this.config = {
      consumerKey: process.env.MPESA_CONSUMER_KEY || '',
      consumerSecret: process.env.MPESA_CONSUMER_SECRET || '',
      businessShortCode: process.env.MPESA_SHORTCODE || '174379',
      passkey: process.env.MPESA_PASSKEY || '',
      callbackUrl: process.env.MPESA_CALLBACK_URL || 'https://api.somolink.ke/api/v1/mpesa/callback',
      environment: (process.env.MPESA_ENV as 'sandbox' | 'production') || 'sandbox',
    };

    this.baseUrl = this.config.environment === 'sandbox' 
      ? 'https://sandbox.safaricom.co.ke' 
      : 'https://api.safaricom.co.ke';
  }

  /**
   * Get OAuth access token from M-Pesa API
   */
  private async getAccessToken(): Promise<string> {
    // Return cached token if still valid
    if (this.accessToken && Date.now() < this.tokenExpiry) {
      return this.accessToken;
    }

    try {
      const auth = Buffer.from(
        `${this.config.consumerKey}:${this.config.consumerSecret}`
      ).toString('base64');

      const response = await axios.get(
        `${this.baseUrl}/oauth/v1/generate?grant_type=client_credentials`,
        {
          headers: {
            Authorization: `Basic ${auth}`,
          },
        }
      );

      this.accessToken = response.data.access_token;
      // Token expires in 3599 seconds, cache for 3000 to be safe
      this.tokenExpiry = Date.now() + 3000 * 1000;

      logger.info('M-Pesa access token obtained successfully');
      return this.accessToken;
    } catch (error) {
      logger.error('Failed to get M-Pesa access token:', error);
      throw new Error('M-Pesa authentication failed');
    }
  }

  /**
   * Generate password for STK Push
   */
  private generatePassword(): { password: string; timestamp: string } {
    const timestamp = new Date()
      .toISOString()
      .replace(/[-:TZ.]/g, '')
      .slice(0, 14);

    const password = Buffer.from(
      `${this.config.businessShortCode}${this.config.passkey}${timestamp}`
    ).toString('base64');

    return { password, timestamp };
  }

  /**
   * Format phone number to M-Pesa format (254XXXXXXXXX)
   */
  private formatPhoneNumber(phone: string): string {
    // Remove any spaces, hyphens, or plus signs
    let formatted = phone.replace(/[\s\-+]/g, '');

    // If starts with 0, replace with 254
    if (formatted.startsWith('0')) {
      formatted = '254' + formatted.slice(1);
    }

    // If starts with 254, it's already correct
    // If starts with 7, prepend 254
    if (!formatted.startsWith('254')) {
      formatted = '254' + formatted;
    }

    return formatted;
  }

  /**
   * Initiate STK Push (Lipa Na M-Pesa Online)
   */
  async initiateSTKPush(request: STKPushRequest): Promise<STKPushResponse> {
    try {
      const accessToken = await this.getAccessToken();
      const { password, timestamp } = this.generatePassword();
      const phoneNumber = this.formatPhoneNumber(request.phoneNumber);

      const payload = {
        BusinessShortCode: this.config.businessShortCode,
        Password: password,
        Timestamp: timestamp,
        TransactionType: 'CustomerPayBillOnline',
        Amount: Math.round(request.amount),
        PartyA: phoneNumber,
        PartyB: this.config.businessShortCode,
        PhoneNumber: phoneNumber,
        CallBackURL: this.config.callbackUrl,
        AccountReference: request.accountReference,
        TransactionDesc: request.transactionDesc,
      };

      logger.info('Initiating M-Pesa STK Push', { 
        phoneNumber, 
        amount: request.amount,
        reference: request.accountReference 
      });

      const response = await axios.post(
        `${this.baseUrl}/mpesa/stkpush/v1/processrequest`,
        payload,
        {
          headers: {
            Authorization: `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
          },
        }
      );

      logger.info('STK Push initiated successfully', {
        merchantRequestId: response.data.MerchantRequestID,
        checkoutRequestId: response.data.CheckoutRequestID,
      });

      return response.data;
    } catch (error: any) {
      logger.error('STK Push failed:', error.response?.data || error.message);
      throw new Error('Failed to initiate M-Pesa payment');
    }
  }

  /**
   * Query STK Push transaction status
   */
  async querySTKPushStatus(checkoutRequestId: string): Promise<any> {
    try {
      const accessToken = await this.getAccessToken();
      const { password, timestamp } = this.generatePassword();

      const payload = {
        BusinessShortCode: this.config.businessShortCode,
        Password: password,
        Timestamp: timestamp,
        CheckoutRequestID: checkoutRequestId,
      };

      const response = await axios.post(
        `${this.baseUrl}/mpesa/stkpushquery/v1/query`,
        payload,
        {
          headers: {
            Authorization: `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
          },
        }
      );

      return response.data;
    } catch (error) {
      logger.error('STK Push query failed:', error);
      throw new Error('Failed to query M-Pesa transaction status');
    }
  }

  /**
   * Process M-Pesa callback
   */
  async processCallback(callbackData: any): Promise<{
    success: boolean;
    transactionId?: string;
    amount?: number;
    phoneNumber?: string;
  }> {
    try {
      const stkCallback = callbackData.Body?.stkCallback;

      if (!stkCallback) {
        logger.warn('Invalid M-Pesa callback format');
        return { success: false };
      }

      const resultCode = stkCallback.ResultCode;
      const resultDesc = stkCallback.ResultDesc;

      if (resultCode !== 0) {
        logger.warn('M-Pesa transaction failed', { resultCode, resultDesc });
        return { success: false };
      }

      // Extract callback metadata
      const callbackMetadata = stkCallback.CallbackMetadata?.Item || [];
      const metadata: any = {};

      callbackMetadata.forEach((item: any) => {
        metadata[item.Name] = item.Value;
      });

      const result = {
        success: true,
        transactionId: metadata.MpesaReceiptNumber,
        amount: metadata.Amount,
        phoneNumber: metadata.PhoneNumber,
      };

      logger.info('M-Pesa payment successful', result);
      return result;
    } catch (error) {
      logger.error('Error processing M-Pesa callback:', error);
      return { success: false };
    }
  }

  /**
   * Validate M-Pesa callback signature (for security)
   */
  validateCallback(signature: string, payload: string): boolean {
    // Implement signature validation if M-Pesa provides it
    // For now, return true (should be enhanced for production)
    return true;
  }
}

export default new MPesaService();
